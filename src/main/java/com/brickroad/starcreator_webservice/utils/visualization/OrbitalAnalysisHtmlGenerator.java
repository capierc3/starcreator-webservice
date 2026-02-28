package com.brickroad.starcreator_webservice.utils.visualization;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a self-contained HTML page with an animated orbital simulation
 * and stability analysis for a given StarSystem.
 * <p>
 * Handles single-star, binary (S-type, P-type), and trinary configurations.
 * The output is a complete HTML document with embedded CSS and JavaScript
 * that runs a real-time Keplerian orbit simulation with:
 * - Animated planet positions (Kepler's equation solver)
 * - Gladman stability criterion analysis
 * - Hill sphere calculations
 * - Mean-motion resonance detection
 * - Orbit trail rendering
 * - Top-down and side (inclination) views
 */
public class OrbitalAnalysisHtmlGenerator {

    private static final double SOLAR_MASS_IN_EARTH = 333000.0;
    private static final double GLADMAN_FACTOR = 3.46;

    /**
     * Generate the complete HTML string for a system's orbital analysis.
     */
    public static String generate(StarSystem system) {
        // Group planets by parent star
        Map<String, StarData> starMap = buildStarMap(system);

        // Build the JavaScript data object
        String systemDataJs = buildSystemDataJs(system, starMap);

        // Build sidebar HTML (stability, resonances)
        String sidebarSections = buildSidebarSections(starMap);

        // Build canvas panels (one per star)
        String canvasPanels = buildCanvasPanels(starMap);

        // Assemble
        return assembleHtml(system, systemDataJs, sidebarSections, canvasPanels, starMap);
    }

    // =========================================================================
    // Data extraction
    // =========================================================================

    private static Map<String, StarData> buildStarMap(StarSystem system) {
        // Build a name->Star lookup
        Map<String, Star> starsByName = new LinkedHashMap<>();
        if (system.getStars() != null) {
            system.getStars().stream()
                    .sorted(Comparator.comparing(s -> s.getStarRole() == Star.StarRole.PRIMARY ? 0 : 1))
                    .forEach(s -> starsByName.put(s.getName(), s));
        }

        // Group planets by parent star name
        Map<String, List<Planet>> planetsByStarName = new LinkedHashMap<>();
        if (system.getPlanets() != null) {
            for (Planet planet : system.getPlanets()) {
                String parentName = planet.getParentStar() != null
                        ? planet.getParentStar().getName()
                        : (starsByName.isEmpty() ? "Unknown" : starsByName.keySet().iterator().next());
                planetsByStarName.computeIfAbsent(parentName, k -> new ArrayList<>()).add(planet);
            }
        }

        // Sort each list by SMA
        planetsByStarName.values().forEach(list ->
                list.sort(Comparator.comparingDouble(p -> p.getSemiMajorAxisAU() != null ? p.getSemiMajorAxisAU() : 0.0)));

        // Build StarData entries, keyed by short label (A, B, C or star name for singles)
        Map<String, StarData> result = new LinkedHashMap<>();
        List<String> orderedNames = new ArrayList<>(starsByName.keySet());

        if (orderedNames.size() <= 1) {
            // Single star system
            String name = orderedNames.isEmpty() ? "Primary" : orderedNames.get(0);
            Star star = orderedNames.isEmpty() ? null : starsByName.get(name);
            List<Planet> planets = planetsByStarName.getOrDefault(name, Collections.emptyList());
            List<OrbitalBand> belts = star != null && star.getBands() != null ? star.getBands() : Collections.emptyList();
            result.put("Primary", new StarData(name, "Primary", star, planets, belts));
        } else {
            // Multi-star: extract the suffix letter(s) from each star name
            for (String fullName : orderedNames) {
                Star star = starsByName.get(fullName);
                String label = extractStarLabel(fullName);
                List<Planet> planets = planetsByStarName.getOrDefault(fullName, Collections.emptyList());
                List<OrbitalBand> belts = star != null && star.getBands() != null ? star.getBands() : Collections.emptyList();
                result.put(label, new StarData(fullName, label, star, planets, belts));
            }
        }

        return result;
    }

    /**
     * Extracts the label suffix from a star name like "SCS-V01-25J A" -> "A"
     */
    private static String extractStarLabel(String fullName) {
        if (fullName == null) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : fullName;
    }

    // =========================================================================
    // JavaScript data generation
    // =========================================================================

    private static String buildSystemDataJs(StarSystem system, Map<String, StarData> starMap) {
        StringBuilder sb = new StringBuilder("const SYSTEM = {\n");

        int idx = 0;
        for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
            String key = sanitizeJsKey(entry.getKey());
            StarData sd = entry.getValue();
            double starMass = sd.star != null ? sd.star.getSolarMass() : 1.0;
            String starColor = getStarColor(idx, starMap.size());

            sb.append(String.format("  '%s': {\n", key));
            sb.append(String.format("    starMass: %.6f,\n", starMass));
            sb.append(String.format("    fullName: '%s',\n", escapeJs(sd.fullName)));
            sb.append(String.format("    label: '%s',\n", escapeJs(sd.label)));
            sb.append(String.format("    color: '%s',\n", starColor));
            sb.append("    planets: [\n");

            for (Planet p : sd.planets) {
                double sma = safe(p.getSemiMajorAxisAU(), 1.0);
                double ecc = safe(p.getEccentricity(), 0.0);
                double inc = safe(p.getOrbitalInclinationDegrees(), 0.0);
                double period = safe(p.getOrbitalPeriodDays(), 365.0);
                double mass = safe(p.getEarthMass(), 1.0);
                String pName = extractPlanetLetter(p.getName());
                String pType = p.getPlanetType() != null ? p.getPlanetType() : "Unknown";
                // Prefer persisted surface color; fall back to type-based lookup
                String pColor = p.getSurfaceColorPrimary() != null
                        ? p.getSurfaceColorPrimary()
                        : getPlanetColor(pType);

                sb.append(String.format(
                        "      {name:'%s',type:'%s',mass:%.6f,sma:%.6f,ecc:%.6f,inc:%.4f,period:%.4f,color:'%s'},\n",
                        escapeJs(pName), escapeJs(pType), mass, sma, ecc, inc, period, pColor));
            }

            sb.append("    ],\n");
            sb.append("    belts: [\n");

            for (OrbitalBand belt : sd.belts) {
                double innerSma = belt.getInnerOrbit() != null && belt.getInnerOrbit().getSemiMajorAxis() != null
                        ? belt.getInnerOrbit().getSemiMajorAxis() : 0.0;
                double outerSma = belt.getOuterOrbit() != null && belt.getOuterOrbit().getSemiMajorAxis() != null
                        ? belt.getOuterOrbit().getSemiMajorAxis() : 0.0;
                double innerEcc = belt.getInnerOrbit() != null && belt.getInnerOrbit().getEccentricity() != null
                        ? belt.getInnerOrbit().getEccentricity() : 0.0;
                double outerEcc = belt.getOuterOrbit() != null && belt.getOuterOrbit().getEccentricity() != null
                        ? belt.getOuterOrbit().getEccentricity() : 0.0;
                String bName = belt.getName() != null ? belt.getName() : (belt.getBandType() != null ? belt.getBandType() : "Belt");
                String bType = belt.getBandType() != null ? belt.getBandType() : "Unknown";
                String bComp = belt.getCompositionType() != null ? belt.getCompositionType() : "MIXED";
                String bColor = getBeltColor(bComp);

                sb.append(String.format(
                        "      {name:'%s',type:'%s',comp:'%s',innerSma:%.6f,outerSma:%.6f,innerEcc:%.6f,outerEcc:%.6f,color:'%s'},\n",
                        escapeJs(bName), escapeJs(bType), escapeJs(bComp), innerSma, outerSma, innerEcc, outerEcc, bColor));
            }

            sb.append("    ]\n");
            sb.append("  },\n");
            idx++;
        }

        sb.append("};\n");
        double minPeriod = Double.MAX_VALUE;
        for (StarData sd : starMap.values()) {
            for (Planet p : sd.planets) {
                double period = safe(p.getOrbitalPeriodDays(), 365.0);
                if (period < minPeriod) minPeriod = period;
            }
        }
        sb.append(String.format("const MIN_PERIOD = %.6f;\n", minPeriod));
        return sb.toString();
    }

    // =========================================================================
    // Sidebar sections (stability, resonance)
    // =========================================================================

    private static String buildSidebarSections(Map<String, StarData> starMap) {
        StringBuilder sb = new StringBuilder();

        // Planet lists per star
        for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
            StarData sd = entry.getValue();
            String key = sanitizeJsKey(entry.getKey());
            sb.append(String.format("<div class=\"section\"><div class=\"section-title\">%s Planets</div><div id=\"planetList_%s\"></div></div>\n",
                    escapeHtml(sd.label.equals("Primary") ? "System" : "Star " + sd.label), key));
        }

        // Stability section
        sb.append("<div class=\"section\"><div class=\"section-title\">Orbital Stability (Gladman Criterion)</div>\n");
        for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
            StarData sd = entry.getValue();
            double starMass = sd.star != null ? sd.star.getSolarMass() : 1.0;
            List<Planet> planets = sd.planets;

            for (int i = 0; i < planets.size() - 1; i++) {
                Planet p1 = planets.get(i);
                Planet p2 = planets.get(i + 1);
                StabilityResult sr = computeStability(p1, p2, starMass);
                String label = sd.label.equals("Primary") ? "" : sd.label;
                sb.append(String.format(
                        "<div class=\"stability-row\"><span class=\"pair\">%s%s↔%s%s</span>" +
                                "<span class=\"metric\">gap %.3f / %.3f = %.1f×</span>" +
                                "<span class=\"badge %s\">%s</span></div>\n",
                        label, extractPlanetLetter(p1.getName()),
                        label, extractPlanetLetter(p2.getName()),
                        sr.gap, sr.mutualHill, sr.ratio,
                        sr.badgeClass, sr.badgeText));
            }
        }
        sb.append("</div>\n");

        // Resonance section
        sb.append("<div class=\"section\"><div class=\"section-title\">Period Ratios</div>\n");
        for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
            StarData sd = entry.getValue();
            List<Planet> planets = sd.planets;
            for (int i = 0; i < planets.size() - 1; i++) {
                double period1 = safe(planets.get(i).getOrbitalPeriodDays(), 1.0);
                double period2 = safe(planets.get(i + 1).getOrbitalPeriodDays(), 1.0);
                double ratio = period2 / period1;
                String nearRes = findNearResonance(ratio);
                String label = sd.label.equals("Primary") ? "" : sd.label;
                sb.append(String.format(
                        "<div class=\"resonance-row\"><span>%s%s → %s%s</span><span>%.3f%s</span></div>\n",
                        label, extractPlanetLetter(planets.get(i).getName()),
                        label, extractPlanetLetter(planets.get(i + 1).getName()),
                        ratio, nearRes));
            }
        }
        sb.append("</div>\n");

        // Belt section
        boolean hasBelts = starMap.values().stream().anyMatch(sd -> !sd.belts.isEmpty());
        if (hasBelts) {
            sb.append("<div class=\"section\"><div class=\"section-title\">Belts</div>\n");
            for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
                StarData sd = entry.getValue();
                String starLabel = sd.label.equals("Primary") ? "" : "Star " + sd.label + ": ";
                for (OrbitalBand belt : sd.belts) {
                    double innerSma = belt.getInnerOrbit() != null && belt.getInnerOrbit().getSemiMajorAxis() != null
                            ? belt.getInnerOrbit().getSemiMajorAxis() : 0.0;
                    double outerSma = belt.getOuterOrbit() != null && belt.getOuterOrbit().getSemiMajorAxis() != null
                            ? belt.getOuterOrbit().getSemiMajorAxis() : 0.0;
                    String beltName = belt.getName() != null ? belt.getName() : (belt.getBandType() != null ? belt.getBandType() : "Belt");
                    String comp = belt.getCompositionType() != null ? belt.getCompositionType() : "";
                    String bColor = getBeltColor(belt.getCompositionType());

                    // Check for planet overlaps (exclude dwarf planets — they naturally reside in belts)
                    boolean overlaps = false;
                    for (Planet p : sd.planets) {
                        String pType = p.getPlanetType();
                        if (pType != null && pType.toLowerCase().contains("dwarf")) continue;
                        double sma = safe(p.getSemiMajorAxisAU(), 0.0);
                        double ecc = safe(p.getEccentricity(), 0.0);
                        if (sma > 0) {
                            double peri = sma * (1 - ecc);
                            double apo = sma * (1 + ecc);
                            if (peri < outerSma && apo > innerSma) {
                                overlaps = true;
                                break;
                            }
                        }
                    }

                    sb.append(String.format(
                            "<div class=\"stability-row\"><span class=\"pair\"><span style=\"display:inline-block;width:8px;height:8px;border-radius:50%%;background:%s;margin-right:4px\"></span>%s%s</span>"
                                    + "<span class=\"metric\">%.2f–%.2f AU · %s</span>%s</div>\n",
                            bColor, escapeHtml(starLabel), escapeHtml(beltName),
                            innerSma, outerSma, escapeHtml(comp),
                            overlaps ? "<span class=\"badge badge-warn\">OVERLAP</span>" : ""));
                }
            }
            sb.append("</div>\n");
        }

        return sb.toString();
    }

    // =========================================================================
    // Canvas panels
    // =========================================================================

    private static String buildCanvasPanels(Map<String, StarData> starMap) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (Map.Entry<String, StarData> entry : starMap.entrySet()) {
            String key = sanitizeJsKey(entry.getKey());
            StarData sd = entry.getValue();
            double starMass = sd.star != null ? sd.star.getSolarMass() : 1.0;
            String starColor = getStarColor(idx, starMap.size());
            String borderStyle = (idx < starMap.size() - 1) ? "" : "border-right:none";

            String starLabel = sd.label.equals("Primary")
                    ? String.format("%.3f M☉", starMass)
                    : String.format("Star %s · %.3f M☉", sd.label, starMass);

            sb.append(String.format(
                    "<div class=\"sim-view\" id=\"view_%s\" style=\"%s\">" +
                            "<canvas id=\"canvas_%s\"></canvas>" +
                            "<div class=\"sim-label\"><div class=\"dot\" style=\"background:%s\"></div> %s</div>" +
                            "<div class=\"time-display\" id=\"time_%s\"></div>" +
                            "<div class=\"tooltip\" id=\"tooltip_%s\"></div>" +
                            "</div>\n",
                    key, borderStyle, key, starColor, escapeHtml(starLabel), key, key));
            idx++;
        }
        return sb.toString();
    }

    // =========================================================================
    // Physics calculations
    // =========================================================================

    private static StabilityResult computeStability(Planet p1, Planet p2, double starMassSolar) {
        double sma1 = safe(p1.getSemiMajorAxisAU(), 1.0);
        double ecc1 = safe(p1.getEccentricity(), 0.0);
        double sma2 = safe(p2.getSemiMajorAxisAU(), 1.0);
        double ecc2 = safe(p2.getEccentricity(), 0.0);
        double mass1 = safe(p1.getEarthMass(), 1.0);
        double mass2 = safe(p2.getEarthMass(), 1.0);

        double apo1 = sma1 * (1 + ecc1);
        double peri2 = sma2 * (1 - ecc2);
        double gap = peri2 - apo1;

        double hill1 = sma1 * Math.pow(mass1 / (3 * starMassSolar * SOLAR_MASS_IN_EARTH), 1.0 / 3.0);
        double hill2 = sma2 * Math.pow(mass2 / (3 * starMassSolar * SOLAR_MASS_IN_EARTH), 1.0 / 3.0);
        double mutualHill = GLADMAN_FACTOR * (hill1 + hill2);
        double ratio = mutualHill > 0 ? gap / mutualHill : 999;

        String badgeText;
        String badgeClass;
        if (gap < 0) {
            badgeText = "CROSSING";
            badgeClass = "badge-danger";
        } else if (ratio < 1) {
            badgeText = "UNSTABLE";
            badgeClass = "badge-warn";
        } else if (ratio < 2) {
            badgeText = "MARGINAL";
            badgeClass = "badge-warn";
        } else {
            badgeText = "STABLE";
            badgeClass = "badge-safe";
        }

        return new StabilityResult(gap, mutualHill, ratio, badgeText, badgeClass);
    }

    private static String findNearResonance(double ratio) {
        int[][] candidates = {{2, 1}, {3, 1}, {3, 2}, {4, 3}, {5, 3}, {5, 2}, {7, 4}, {7, 3}};
        for (int[] c : candidates) {
            if (Math.abs(ratio - (double) c[0] / c[1]) < 0.08) {
                return String.format(" ≈ %d:%d", c[0], c[1]);
            }
        }
        return "";
    }

    // =========================================================================
    // Color helpers
    // =========================================================================

    private static String getStarColor(int index, int total) {
        if (total == 1) return "#f0c850";
        String[] colors = {"#6ea8fe", "#ffa94d", "#ff6b9d"};
        return colors[index % colors.length];
    }

    private static String getBeltColor(String compositionType) {
        if (compositionType == null) return "#665544";
        String c = compositionType.toUpperCase();
        if (c.contains("ICY") || c.contains("ICE")) return "#557799";
        if (c.contains("ROCKY") || c.contains("SILICATE")) return "#887766";
        if (c.contains("METALLIC") || c.contains("IRON")) return "#998877";
        if (c.contains("CARBON")) return "#554433";
        return "#665544";
    }

    private static String getPlanetColor(String planetType) {
        if (planetType == null) return "#997755";
        String t = planetType.toLowerCase();
        if (t.contains("gas giant") || t.contains("hot jupiter")) return "#c4956a";
        if (t.contains("super-jupiter")) return "#b08050";
        if (t.contains("ice giant")) return "#4488cc";
        if (t.contains("sub-neptune") || t.contains("mini-neptune")) return "#5b9bd5";
        if (t.contains("warm neptune") || t.contains("hot neptune")) return "#6ea8d4";
        if (t.contains("super-earth")) return "#aa8866";
        if (t.contains("terrestrial")) return "#88aa66";
        if (t.contains("ocean")) return "#3388aa";
        if (t.contains("desert")) return "#cc9955";
        if (t.contains("ice world")) return "#aaccee";
        if (t.contains("lava")) return "#ff4422";
        if (t.contains("iron")) return "#888888";
        if (t.contains("carbon")) return "#555555";
        if (t.contains("dwarf")) return "#8899aa";
        if (t.contains("puffy")) return "#dd8855";
        return "#997755";
    }

    // =========================================================================
    // String helpers
    // =========================================================================

    private static String extractPlanetLetter(String fullName) {
        if (fullName == null) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[parts.length - 1] : fullName;
    }

    private static String sanitizeJsKey(String key) {
        return key.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static double safe(Double val, double fallback) {
        return val != null ? val : fallback;
    }

    // =========================================================================
    // Inner types
    // =========================================================================

    private static class StarData {
        final String fullName;
        final String label;
        final Star star;
        final List<Planet> planets;
        final List<OrbitalBand> belts;

        StarData(String fullName, String label, Star star, List<Planet> planets, List<OrbitalBand> belts) {
            this.fullName = fullName;
            this.label = label;
            this.star = star;
            this.planets = planets;
            this.belts = belts;
        }
    }

    private record StabilityResult(double gap, double mutualHill, double ratio,
                                   String badgeText, String badgeClass) {
    }

    // =========================================================================
    // HTML Template Assembly
    // =========================================================================

    private static String assembleHtml(StarSystem system, String systemDataJs,
                                       String sidebarSections, String canvasPanels,
                                       Map<String, StarData> starMap) {
        String systemName = system.getName() != null ? system.getName() : "Star System";
        BinaryConfiguration config = system.getBinaryConfiguration();
        String configLabel = config != null ? formatBinaryConfig(config, system.getBinarySeparationAu()) : "Single Star";

        // Build the JS key array for canvas init
        String keysArray = starMap.keySet().stream()
                .map(OrbitalAnalysisHtmlGenerator::sanitizeJsKey)
                .map(k -> "'" + k + "'")
                .collect(Collectors.joining(","));

        return HTML_TEMPLATE
                .replace("{{SYSTEM_NAME}}", escapeHtml(systemName))
                .replace("{{CONFIG_LABEL}}", escapeHtml(configLabel))
                .replace("{{CANVAS_PANELS}}", canvasPanels)
                .replace("{{SIDEBAR_SECTIONS}}", sidebarSections)
                .replace("{{SYSTEM_DATA_JS}}", systemDataJs)
                .replace("{{STAR_KEYS_ARRAY}}", keysArray);
    }

    private static String formatBinaryConfig(BinaryConfiguration config, Double separation) {
        String base = switch (config) {
            case SINGLE -> "Single Star";
            case P_TYPE -> "P-Type Binary (circumbinary)";
            case S_TYPE_WIDE -> "S-Type Wide Binary";
            case S_TYPE_CLOSE -> "S-Type Close Binary";
            case HIERARCHICAL_BINARY_THIRD -> "Hierarchical Binary + Third";
            case HIERARCHICAL_TRIPLE -> "Hierarchical Triple";
        };
        if (separation != null && config != BinaryConfiguration.SINGLE) {
            base += String.format(" · %.0f AU sep", separation);
        }
        return base;
    }

    // =========================================================================
    // The HTML template — self-contained with CSS + JS
    // =========================================================================

    private static final String HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{{SYSTEM_NAME}} — Orbital Analysis</title>
<style>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@300;400;500;700&family=Outfit:wght@200;300;400;600;700&display=swap');
*{margin:0;padding:0;box-sizing:border-box}
:root{--bg:#0a0c10;--panel:#0f1218;--border:#1a1f2a;--text:#c8ccd4;--dim:#5a6070;--accent:#ff6b35;--safe:#34d399;--warn:#f59e0b;--danger:#ef4444}
body{background:var(--bg);color:var(--text);font-family:'Outfit',sans-serif;overflow:hidden;height:100vh;display:flex;flex-direction:column}
.header{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--border);background:linear-gradient(180deg,#0f1218 0%,var(--bg) 100%);flex-shrink:0}
.header h1{font-size:16px;font-weight:600;letter-spacing:.5px}
.header h1 span{color:var(--dim);font-weight:300;margin-left:8px;font-size:13px}
.controls{display:flex;align-items:center;gap:12px}
.controls button{background:var(--panel);border:1px solid var(--border);color:var(--text);padding:5px 14px;border-radius:4px;cursor:pointer;font-family:'JetBrains Mono',monospace;font-size:11px;transition:all .15s}
.controls button:hover{border-color:var(--accent);color:#fff}
.controls button.active{background:var(--accent);border-color:var(--accent);color:#fff}
.speed-control{display:flex;align-items:center;gap:6px;font-family:'JetBrains Mono',monospace;font-size:11px;color:var(--dim)}
.speed-control input[type="range"]{width:80px;accent-color:var(--accent);height:3px}
.main{display:flex;flex:1;overflow:hidden}
.sim-container{flex:1;position:relative;display:flex}
.sim-view{flex:1;position:relative;border-right:1px solid var(--border)}
.sim-view canvas{width:100%;height:100%;display:block}
.sim-label{position:absolute;top:10px;left:14px;font-family:'JetBrains Mono',monospace;font-size:11px;font-weight:500;display:flex;align-items:center;gap:6px;pointer-events:none}
.sim-label .dot{width:8px;height:8px;border-radius:50%}
.time-display{position:absolute;bottom:10px;left:14px;font-family:'JetBrains Mono',monospace;font-size:10px;color:var(--dim);pointer-events:none}
.sidebar{width:340px;flex-shrink:0;overflow-y:auto;border-left:1px solid var(--border);background:var(--panel)}
.sidebar::-webkit-scrollbar{width:4px}
.sidebar::-webkit-scrollbar-track{background:transparent}
.sidebar::-webkit-scrollbar-thumb{background:var(--border);border-radius:2px}
.section{padding:14px 16px;border-bottom:1px solid var(--border)}
.section-title{font-family:'JetBrains Mono',monospace;font-size:10px;text-transform:uppercase;letter-spacing:1.2px;color:var(--dim);margin-bottom:10px}
.planet-row{display:grid;grid-template-columns:70px 1fr;gap:4px 10px;margin-bottom:8px;padding:6px 8px;border-radius:4px;transition:background .15s}
.planet-row:hover{background:rgba(255,255,255,.03)}
.planet-name{font-family:'JetBrains Mono',monospace;font-size:11px;font-weight:500;display:flex;align-items:center;gap:5px}
.planet-name .pip{width:6px;height:6px;border-radius:50%;flex-shrink:0}
.planet-meta{font-size:11px;color:var(--dim)}
.planet-type{font-size:10px;color:var(--dim);grid-column:1/-1}
.orbit-bar-container{grid-column:1/-1;height:14px;background:rgba(255,255,255,.03);border-radius:2px;position:relative;margin-top:2px;overflow:hidden}
.orbit-bar-range{position:absolute;top:2px;bottom:2px;border-radius:1px;opacity:.5}
.orbit-bar-sma{position:absolute;top:0;bottom:0;width:2px;transform:translateX(-1px)}
.stability-row{display:flex;align-items:center;justify-content:space-between;padding:5px 8px;margin-bottom:4px;border-radius:3px;font-family:'JetBrains Mono',monospace;font-size:10px}
.stability-row .pair{color:var(--text)}
.stability-row .metric{color:var(--dim)}
.badge{padding:1px 6px;border-radius:2px;font-weight:500;font-size:9px}
.badge-safe{background:rgba(52,211,153,.15);color:var(--safe)}
.badge-warn{background:rgba(245,158,11,.15);color:var(--warn)}
.badge-danger{background:rgba(239,68,68,.15);color:var(--danger)}
.resonance-row{display:flex;align-items:center;justify-content:space-between;padding:4px 8px;font-family:'JetBrains Mono',monospace;font-size:10px;color:var(--dim)}
.tooltip{position:absolute;background:#181c24;border:1px solid var(--border);padding:6px 10px;border-radius:4px;font-family:'JetBrains Mono',monospace;font-size:10px;pointer-events:none;z-index:10;display:none;white-space:nowrap;box-shadow:0 4px 16px rgba(0,0,0,.5)}
.view-tabs{display:flex;gap:2px}
.view-tabs button{font-size:10px;padding:4px 10px}
</style>
</head>
<body>
<div class="header">
  <h1>{{SYSTEM_NAME}} <span>{{CONFIG_LABEL}}</span></h1>
  <div class="controls">
    <div class="view-tabs">
      <button id="btnTop" class="active" onclick="setView('top')">Top</button>
      <button id="btnSide" onclick="setView('side')">Side</button>
    </div>
    <button id="btnPlay" class="active" onclick="togglePlay()">&#9654; Running</button>
    <button id="btnTrails" onclick="toggleTrails()">Trails</button>
    <div class="speed-control">
      <span>Speed</span>
      <input type="range" id="speedSlider" min="0" max="5" step="0.1" value="2">
      <span id="speedLabel">&times;1.00</span>
    </div>
  </div>
</div>
<div class="main">
  <div class="sim-container">
    {{CANVAS_PANELS}}
  </div>
  <div class="sidebar">
    {{SIDEBAR_SECTIONS}}
  </div>
</div>
<script>
{{SYSTEM_DATA_JS}}
const STAR_KEYS = [{{STAR_KEYS_ARRAY}}];

// Precompute derived orbital values
for (const key of STAR_KEYS) {
  const sys = SYSTEM[key];
  for (const p of sys.planets) {
    p.peri = p.sma * (1 - p.ecc);
    p.apo = p.sma * (1 + p.ecc);
    p.hill = p.sma * Math.pow(p.mass / (3 * sys.starMass * 333000), 1/3);
    p.phase = Math.random() * Math.PI * 2;
    p.trail = [];
    p.renderSize = Math.max(2.5, Math.min(7, 2 + Math.log10(Math.max(0.01, p.mass)) * 2));
  }
}

// State
let playing = true, showTrails = false, viewMode = 'top', simTime = 0, speedFactor = 1;

// Canvas setup
const canvases = {};
function setupCanvas(key) {
  const canvas = document.getElementById('canvas_' + key);
  if (!canvas) return null;
  const container = canvas.parentElement;
  const dpr = window.devicePixelRatio || 1;
  function resize() {
    const r = container.getBoundingClientRect();
    canvas.width = r.width * dpr;
    canvas.height = r.height * dpr;
    canvas.style.width = r.width + 'px';
    canvas.style.height = r.height + 'px';
  }
  resize();
  window.addEventListener('resize', resize);
  return { canvas, ctx: canvas.getContext('2d'), dpr };
}
for (const key of STAR_KEYS) { canvases[key] = setupCanvas(key); }

function solveKepler(M, e) {
  let E = M;
  for (let i = 0; i < 30; i++) {
    const dE = (M - E + e * Math.sin(E)) / (1 - e * Math.cos(E));
    E += dE;
    if (Math.abs(dE) < 1e-10) break;
  }
  return E;
}

function getOrbitalPos(p, t) {
  const M = p.phase + (2 * Math.PI / p.period) * t;
  const E = solveKepler(M % (2*Math.PI), p.ecc);
  const v = 2 * Math.atan2(Math.sqrt(1+p.ecc)*Math.sin(E/2), Math.sqrt(1-p.ecc)*Math.cos(E/2));
  const r = p.sma * (1 - p.ecc * Math.cos(E));
  const x = r * Math.cos(v), y0 = r * Math.sin(v);
  const inc = p.inc * Math.PI / 180;
  return { x, y: y0 * Math.cos(inc), z: y0 * Math.sin(inc), r };
}

function drawSystem(cv, sysKey, t) {
  if (!cv) return;
  const { canvas, ctx, dpr } = cv;
  const sys = SYSTEM[sysKey];
  const w = canvas.width, h = canvas.height;
  ctx.clearRect(0, 0, w, h);

  const bgG = ctx.createRadialGradient(w/2,h/2,0,w/2,h/2,w*.6);
  bgG.addColorStop(0,'#0d0f14'); bgG.addColorStop(1,'#0a0c10');
  ctx.fillStyle = bgG; ctx.fillRect(0,0,w,h);

  if (sys.planets.length === 0 && sys.belts.length === 0) {
    ctx.fillStyle = '#ffffff30';
    ctx.font = 12*dpr + "px 'Outfit'";
    ctx.textAlign = 'center';
    ctx.fillText('No planets or belts', w/2, h/2);
    ctx.textAlign = 'start';
    return;
  }

  // Include belt outer edges in max orbit calculation
  let maxOrbit = sys.planets.length > 0 ? sys.planets[sys.planets.length-1].apo : 0;
  for (const b of sys.belts) {
    const bOuter = b.outerSma * (1 + b.outerEcc);
    if (bOuter > maxOrbit) maxOrbit = bOuter;
  }
  if (maxOrbit <= 0) maxOrbit = 1;
  const scale = Math.min(w,h) * 0.38 / maxOrbit * dpr;
  const cx = w/2, cy = h/2;

  function toScreen(x,y,z) {
    return viewMode === 'top' ? [cx+x*scale, cy+y*scale] : [cx+x*scale, cy+z*scale*3];
  }

  // Belt annuli (draw behind orbits and planets)
  if (viewMode === 'top') {
    for (const b of sys.belts) {
      const innerR = b.innerSma * scale;
      const outerR = b.outerSma * scale;
      if (outerR > 0) {
        ctx.save();
        ctx.beginPath();
        ctx.arc(cx, cy, outerR, 0, Math.PI*2);
        ctx.arc(cx, cy, innerR, 0, Math.PI*2, true);
        ctx.fillStyle = b.color + '18';
        ctx.fill();
        ctx.strokeStyle = b.color + '35';
        ctx.lineWidth = 1 * dpr;
        ctx.beginPath();
        ctx.arc(cx, cy, outerR, 0, Math.PI*2);
        ctx.stroke();
        ctx.beginPath();
        ctx.arc(cx, cy, innerR, 0, Math.PI*2);
        ctx.stroke();
        ctx.restore();
      }
    }
  } else {
    // Side view: draw belts as thin horizontal bands
    for (const b of sys.belts) {
      const innerR = b.innerSma * scale;
      const outerR = b.outerSma * scale;
      if (outerR > 0) {
        ctx.save();
        ctx.fillStyle = b.color + '18';
        ctx.fillRect(cx - outerR, cy - 2*dpr, outerR - innerR, 4*dpr);
        ctx.fillRect(cx + innerR, cy - 2*dpr, outerR - innerR, 4*dpr);
        ctx.restore();
      }
    }
  }

  // Orbit ellipses
  for (const p of sys.planets) {
    ctx.save(); ctx.translate(cx, cy); ctx.beginPath();
    if (viewMode === 'top') {
      const a = p.sma*scale, b = a*Math.sqrt(1-p.ecc*p.ecc), cF = p.sma*p.ecc*scale;
      ctx.ellipse(-cF, 0, a, b, 0, 0, Math.PI*2);
    } else {
      const inc = p.inc*Math.PI/180, steps = 120;
      for (let i = 0; i <= steps; i++) {
        const angle = (i/steps)*Math.PI*2;
        const ox = (p.sma*(Math.cos(angle)-p.ecc))*scale;
        const oy = p.sma*Math.sqrt(1-p.ecc*p.ecc)*Math.sin(angle);
        const oz = oy*Math.sin(inc)*scale*3;
        i === 0 ? ctx.moveTo(ox,oz) : ctx.lineTo(ox,oz);
      }
      ctx.closePath();
    }
    ctx.strokeStyle = p.color + '20'; ctx.lineWidth = 1*dpr; ctx.stroke(); ctx.restore();
  }

  // Trails
  if (showTrails) {
    for (const p of sys.planets) {
      if (p.trail.length < 2) continue;
      ctx.beginPath();
      for (let i = 0; i < p.trail.length; i++) {
        const [sx,sy] = toScreen(p.trail[i].x, p.trail[i].y, p.trail[i].z);
        i === 0 ? ctx.moveTo(sx,sy) : ctx.lineTo(sx,sy);
      }
      ctx.strokeStyle = p.color+'66'; ctx.lineWidth = 1.2*dpr; ctx.stroke();
    }
  }

  // Star
  const sG = ctx.createRadialGradient(cx,cy,0,cx,cy,10*dpr);
  sG.addColorStop(0, sys.color); sG.addColorStop(.5, sys.color+'80'); sG.addColorStop(1, sys.color+'00');
  ctx.fillStyle = sG; ctx.beginPath(); ctx.arc(cx,cy,10*dpr,0,Math.PI*2); ctx.fill();
  ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(cx,cy,3*dpr,0,Math.PI*2); ctx.fill();

  // Planets
  const positions = [];
  for (const p of sys.planets) {
    const pos = getOrbitalPos(p, t);
    const [sx,sy] = toScreen(pos.x, pos.y, pos.z);
    positions.push({p,sx,sy,pos});

    if (playing) {
      p.trail.push({x:pos.x,y:pos.y,z:pos.z});
      const maxLen = Math.max(200, Math.floor(p.period / (speedFactor||1) * 0.15));
      if (p.trail.length > maxLen) p.trail.shift();
    }

    const glowR = (p.renderSize+4)*dpr;
    const glow = ctx.createRadialGradient(sx,sy,0,sx,sy,glowR);
    glow.addColorStop(0, p.color+'40'); glow.addColorStop(1, p.color+'00');
    ctx.fillStyle = glow; ctx.beginPath(); ctx.arc(sx,sy,glowR,0,Math.PI*2); ctx.fill();

    ctx.fillStyle = p.color; ctx.beginPath(); ctx.arc(sx,sy,p.renderSize*dpr,0,Math.PI*2); ctx.fill();
    ctx.fillStyle = '#ffffff90'; ctx.font = 10*dpr+"px 'JetBrains Mono'";
    ctx.fillText(p.name, sx+(p.renderSize+4)*dpr, sy-4*dpr);
  }

  // Warning lines for close approaches
  for (let i = 0; i < positions.length-1; i++) {
    const a = positions[i], b = positions[i+1];
    const dx = b.pos.x-a.pos.x, dy = b.pos.y-a.pos.y, dz = b.pos.z-a.pos.z;
    const dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
    const bigHill = Math.max(a.p.hill, b.p.hill);
    if (dist < bigHill*5) {
      ctx.beginPath(); ctx.moveTo(a.sx,a.sy); ctx.lineTo(b.sx,b.sy);
      const alpha = Math.min(1, bigHill*3/dist);
      ctx.strokeStyle = dist < bigHill ? 'rgba(239,68,68,'+alpha+')' : 'rgba(245,158,11,'+alpha*.6+')';
      ctx.lineWidth = 1.5*dpr; ctx.setLineDash([4*dpr,4*dpr]); ctx.stroke(); ctx.setLineDash([]);
    }
  }

  // Belt labels (top view only)
  if (viewMode === 'top') {
    ctx.font = 9*dpr+"px 'JetBrains Mono'";
    for (const b of sys.belts) {
      const midR = ((b.innerSma + b.outerSma) / 2) * scale;
      ctx.fillStyle = b.color + '80';
      ctx.fillText(b.type, cx + midR * 0.7 + 4*dpr, cy - midR * 0.7 - 4*dpr);
    }
  }
}

// Build planet list HTML in sidebar
function buildPlanetLists() {
  for (const key of STAR_KEYS) {
    const sys = SYSTEM[key];
    const el = document.getElementById('planetList_' + key);
    if (!el || sys.planets.length === 0) continue;
    const maxAU = sys.planets[sys.planets.length-1].apo;
    let html = '';
    for (const p of sys.planets) {
      const periPct = (p.peri/maxAU*100).toFixed(1);
      const apoPct = (p.apo/maxAU*100).toFixed(1);
      const smaPct = (p.sma/maxAU*100).toFixed(1);
      html += '<div class="planet-row">' +
        '<div class="planet-name"><div class="pip" style="background:'+p.color+'"></div>'+p.name+'</div>' +
        '<div class="planet-meta">'+p.sma.toFixed(3)+' AU · e='+p.ecc.toFixed(3)+'</div>' +
        '<div class="planet-type">'+p.type+' · '+p.mass.toFixed(2)+' M⊕ · P='+(p.period/365.25).toFixed(2)+' yr</div>' +
        '<div class="orbit-bar-container">' +
          '<div class="orbit-bar-range" style="left:'+periPct+'%;right:'+(100-apoPct)+'%;background:'+p.color+'"></div>' +
          '<div class="orbit-bar-sma" style="left:'+smaPct+'%;background:'+p.color+'"></div>' +
        '</div></div>';
    }
    el.innerHTML = html;
  }
}

// Controls
function togglePlay() {
  playing = !playing;
  const b = document.getElementById('btnPlay');
  b.innerHTML = playing ? '&#9654; Running' : '&#9208; Paused';
  b.classList.toggle('active', playing);
}
function toggleTrails() {
  showTrails = !showTrails;
  document.getElementById('btnTrails').classList.toggle('active', showTrails);
  if (!showTrails) for (const k of STAR_KEYS) for (const p of SYSTEM[k].planets) p.trail = [];
}
function setView(m) {
  viewMode = m;
  document.getElementById('btnTop').classList.toggle('active', m==='top');
  document.getElementById('btnSide').classList.toggle('active', m==='side');
}
document.getElementById('speedSlider').addEventListener('input', e => {
  speedFactor = Math.pow(2, parseFloat(e.target.value) - 2);
  document.getElementById('speedLabel').textContent = '\\u00d7' + speedFactor.toFixed(2);
});

// Hover tooltips
for (const key of STAR_KEYS) {
  const canvas = document.getElementById('canvas_' + key);
  const tooltip = document.getElementById('tooltip_' + key);
  if (!canvas || !tooltip) continue;
  canvas.addEventListener('mousemove', e => {
    const rect = canvas.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    const sys = SYSTEM[key]; let found = null;
    const dpr = window.devicePixelRatio || 1;
    if (sys.planets.length === 0) { tooltip.style.display='none'; return; }
    const maxO = sys.planets[sys.planets.length-1].apo;
    const sc = Math.min(canvas.width, canvas.height) * 0.38 / maxO;
    const cxc = canvas.width/(2*dpr), cyc = canvas.height/(2*dpr);
    for (const p of sys.planets) {
      const pos = getOrbitalPos(p, simTime);
      let sx,sy;
      if (viewMode==='top'){sx=cxc+pos.x*sc;sy=cyc+pos.y*sc}else{sx=cxc+pos.x*sc;sy=cyc+pos.z*sc*3}
      if (Math.sqrt((mx-sx)**2+(my-sy)**2)<15){found=p;break}
    }
    if (found) {
      const pos = getOrbitalPos(found, simTime);
      tooltip.style.display='block'; tooltip.style.left=(mx+14)+'px'; tooltip.style.top=(my-10)+'px';
      tooltip.innerHTML='<b>'+found.name+'</b> · '+found.type+'<br>r = '+pos.r.toFixed(4)+' AU · e = '+found.ecc+'<br>['+found.peri.toFixed(3)+' – '+found.apo.toFixed(3)+'] AU';
    } else { tooltip.style.display='none'; }
  });
  canvas.addEventListener('mouseleave', () => { tooltip.style.display='none'; });
}

// Animation loop
let lastTime = performance.now();
function animate(now) {
  const dt = Math.min(50, now - lastTime); lastTime = now;
  const timeRate = Math.min(0.05, MIN_PERIOD / 5000);
  if (playing) simTime += dt * timeRate * speedFactor;
  for (const key of STAR_KEYS) drawSystem(canvases[key], key, simTime);
  const years = (simTime / 365.25).toFixed(1);
  for (const key of STAR_KEYS) {
    const el = document.getElementById('time_' + key);
    if (el) el.textContent = 'T = ' + years + ' yr';
  }
  requestAnimationFrame(animate);
}

buildPlanetLists();
requestAnimationFrame(animate);

window.addEventListener('resize', () => {
  for (const key of STAR_KEYS) {
    const cv = canvases[key];
    if (!cv) continue;
    const r = cv.canvas.parentElement.getBoundingClientRect();
    cv.canvas.width = r.width * cv.dpr;
    cv.canvas.height = r.height * cv.dpr;
  }
});
</script>
</body>
</html>
""";
}
