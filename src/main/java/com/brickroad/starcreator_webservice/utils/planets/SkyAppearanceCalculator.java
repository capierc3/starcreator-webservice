package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField.AuroralFrequency;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField.AuroralIntensity;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SkyAppearanceCalculator {

    // Reference constants
    private static final double SOLAR_RADIUS_KM = 695700.0;
    private static final double AU_TO_KM = 1.496e8;
    private static final double EARTH_MOON_ANGULAR_DIAMETER_DEG = 0.52;
    private static final double EARTH_RADIUS_KM = CelestialBodyUtils.EARTH_RADIUS_KM;

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryWeather weather, Planet planet, Star parentStar, StarSystem system) {
        String atmClass = planet.getAtmosphereClassification();

        // Gas giants have no surface — skip moon visibility and eclipses
        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            generateGasGiantSkyDescription(weather, planet, parentStar);
            return;
        }

        // Moon visibility from planet surface
        List<Moon> moons = planet.getMoons();
        if (moons != null && !moons.isEmpty()) {
            calculateMoonVisibility(weather, planet, moons, parentStar);
            calculateEclipses(weather, planet, moons, parentStar);
        }

        // Sky descriptions
        generateSkyDescriptions(weather, planet, parentStar, system);
    }

    // ================================================================
    // MOON VISIBILITY
    // ================================================================

    private void calculateMoonVisibility(PlanetaryWeather weather, Planet planet,
                                          List<Moon> moons, Star parentStar) {
        List<MoonSkyAppearance> appearances = new ArrayList<>();
        double planetDistAU = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 1.0;
        double starLuminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;

        for (Moon moon : moons) {
            Double moonRadiusEarth = moon.getEarthRadius();
            Double moonDistKm = moon.getSemiMajorAxisKm();
            Double moonPeriodDays = moon.getOrbitalPeriodDays();
            Double moonAlbedo = moon.getAlbedo();

            if (moonRadiusEarth == null || moonDistKm == null || moonDistKm <= 0) continue;

            double moonRadiusKm = moonRadiusEarth * EARTH_RADIUS_KM;

            MoonSkyAppearance appearance = new MoonSkyAppearance();
            appearance.setMoonId(moon.getId());

            // Angular diameter: 2 * arctan(radius / distance) in degrees
            double angularDiameterRad = 2.0 * Math.atan(moonRadiusKm / moonDistKm);
            double angularDiameterDeg = Math.toDegrees(angularDiameterRad);
            appearance.setAngularDiameterDegrees(round4(angularDiameterDeg));

            // Phase cycle: synodic period
            // For simplicity, use the orbital period as the phase cycle
            // (synodic correction is small for most systems)
            if (moonPeriodDays != null) {
                appearance.setPhaseCycleDays(round2(moonPeriodDays));
            }

            // Brightness relative to Earth's full Moon
            // B ∝ (albedo × radius² × stellar_flux) / distance²
            // Normalize to Earth's Moon: albedo 0.12, radius 1737km, distance 384400km, flux 1.0
            double albedo = moonAlbedo != null ? moonAlbedo : 0.15;
            double stellarFlux = starLuminosity / (planetDistAU * planetDistAU);
            double brightness = (albedo / 0.12)
                    * Math.pow(moonRadiusKm / 1737.0, 2.0)
                    * (stellarFlux / 1.0)
                    / Math.pow(moonDistKm / 384400.0, 2.0);
            appearance.setBrightnessRelativeToFullMoon(round2(brightness));

            // Visible in daytime? If angular diameter is large enough and bright enough
            appearance.setIsVisibleInDaytime(angularDiameterDeg > 0.1 && brightness > 0.3);

            // Apparent color: mostly determined by albedo and surface composition
            appearance.setApparentColor(estimateMoonColor(albedo));

            // Description
            appearance.setDescription(describeMoonAppearance(angularDiameterDeg, brightness, moonPeriodDays));

            appearances.add(appearance);
        }

        weather.setMoonSkyAppearances(appearances);
    }

    private String estimateMoonColor(double albedo) {
        if (albedo > 0.6) return "Bright white";
        if (albedo > 0.4) return "Pale grey";
        if (albedo > 0.2) return "Silver-grey";
        if (albedo > 0.1) return "Dark grey";
        return "Very dark";
    }

    private String describeMoonAppearance(double angularDeg, double brightness, Double periodDays) {
        StringBuilder desc = new StringBuilder();

        // Size comparison to Earth's Moon
        double sizeRatio = angularDeg / EARTH_MOON_ANGULAR_DIAMETER_DEG;
        if (sizeRatio > 5.0) {
            desc.append("Dominates the sky at ").append(String.format("%.1fx", sizeRatio))
                    .append(" the apparent size of Earth's Moon");
        } else if (sizeRatio > 2.0) {
            desc.append("Appears ").append(String.format("%.1fx", sizeRatio))
                    .append(" larger than Earth's Moon");
        } else if (sizeRatio > 0.5) {
            desc.append("Similar apparent size to Earth's Moon");
        } else if (sizeRatio > 0.1) {
            desc.append("Small but clearly visible disk");
        } else {
            desc.append("Point-like — visible as a bright star");
        }

        // Brightness note
        if (brightness > 10) {
            desc.append(", brilliant enough to cast strong shadows");
        } else if (brightness > 3) {
            desc.append(", bright enough to read by");
        } else if (brightness > 1) {
            desc.append(", prominent in the night sky");
        }

        return desc.toString();
    }

    // ================================================================
    // ECLIPSES
    // ================================================================

    private void calculateEclipses(PlanetaryWeather weather, Planet planet,
                                    List<Moon> moons, Star parentStar) {
        List<EclipseData> eclipses = new ArrayList<>();
        double planetDistAU = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 1.0;
        double starRadiusKm = parentStar.getSolarRadius() * SOLAR_RADIUS_KM;

        // Star angular diameter from planet surface
        double starDistKm = planetDistAU * AU_TO_KM;
        double starAngularDeg = Math.toDegrees(2.0 * Math.atan(starRadiusKm / starDistKm));

        for (Moon moon : moons) {
            Double moonRadiusEarth = moon.getEarthRadius();
            Double moonDistKm = moon.getSemiMajorAxisKm();

            if (moonRadiusEarth == null || moonDistKm == null || moonDistKm <= 0) continue;

            double moonRadiusKm = moonRadiusEarth * EARTH_RADIUS_KM;
            double moonAngularDeg = Math.toDegrees(2.0 * Math.atan(moonRadiusKm / moonDistKm));

            // Eclipse type
            String eclipseType;
            if (moonAngularDeg >= starAngularDeg * 1.05) {
                eclipseType = "TOTAL";
            } else if (moonAngularDeg >= starAngularDeg * 0.9) {
                // Close to same size — could be total or annular depending on orbital position
                eclipseType = RandomUtils.flipCoin() == 1 ? "TOTAL" : "ANNULAR";
            } else if (moonAngularDeg >= starAngularDeg * 0.3) {
                eclipseType = "ANNULAR";
            } else {
                eclipseType = "TRANSIT";
            }

            // Eclipse frequency: rough geometric probability
            // P_eclipse ≈ (R_star + R_moon) / (a_moon × sin(inclination))
            // Without inclination data, use a heuristic: closer/larger moons → more frequent
            double sizeRatio = moonAngularDeg / starAngularDeg;
            double proximityFactor = 384400.0 / moonDistKm; // Closer → more likely
            double frequencyPerYear = sizeRatio * proximityFactor * RandomUtils.rollRange(0.1, 0.5);
            frequencyPerYear = Math.max(0.01, Math.min(50.0, frequencyPerYear));

            // Duration: proportional to angular speed and angular size
            double moonPeriodDays = moon.getOrbitalPeriodDays() != null ? moon.getOrbitalPeriodDays() : 27.3;
            double angularSpeedDegPerMin = 360.0 / (moonPeriodDays * 24.0 * 60.0);
            double durationMinutes = (starAngularDeg + moonAngularDeg) / Math.max(0.001, angularSpeedDegPerMin);
            durationMinutes = Math.max(0.5, Math.min(600.0, durationMinutes));

            EclipseData eclipse = new EclipseData();
            eclipse.setEclipseSource("MOON_SOLAR");
            eclipse.setSourceBodyName(moon.getName() != null ? moon.getName() : "Moon");
            eclipse.setEclipseType(eclipseType);
            eclipse.setFrequencyPerYear(round2(frequencyPerYear));
            eclipse.setTypicalDurationMinutes(round2(durationMinutes));
            eclipse.setDescription(describeEclipse(eclipseType, moonAngularDeg, starAngularDeg));

            eclipses.add(eclipse);
        }

        weather.setEclipseData(eclipses);
    }

    private String describeEclipse(String type, double moonAngularDeg, double starAngularDeg) {
        switch (type) {
            case "TOTAL":
                double coronaRatio = moonAngularDeg / starAngularDeg;
                if (coronaRatio > 2.0) {
                    return "Dramatic total eclipses with extended darkness as the large moon " +
                            "completely obscures the star and its corona";
                }
                return "Total eclipses reveal the stellar corona, similar to Earth's solar eclipses";
            case "ANNULAR":
                return "Annular eclipses create a bright ring of starlight around the silhouetted moon";
            case "TRANSIT":
                return "Small moon transits across the stellar disk as a dark dot, " +
                        "visible to the naked eye but not causing significant dimming";
            default:
                return "Partial eclipses occur when orbital alignment is imprecise";
        }
    }

    // ================================================================
    // SKY DESCRIPTIONS
    // ================================================================

    private void generateSkyDescriptions(PlanetaryWeather weather, Planet planet,
                                          Star parentStar, StarSystem system) {
        String atmClass = planet.getAtmosphereClassification();
        String skyColor = weather.getSkyColor() != null ? weather.getSkyColor() : "BLUE";
        double cloudCoverage = weather.getCloudCoveragePercent() != null ? weather.getCloudCoveragePercent() : 50.0;
        String spectralType = parentStar.getSpectralType() != null ? parentStar.getSpectralType() : "G";
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());

        // Magnetic field info for aurora
        PlanetaryMagneticField magField = planet.getMagneticField();
        boolean hasAurora = magField != null && magField.getAuroralFrequency() != null
                && magField.getAuroralFrequency() != AuroralFrequency.RARE;

        // Daytime sky
        weather.setDaytimeSkyDescription(buildDaytimeDescription(
                skyColor, cloudCoverage, spectralType, atmClass, tidallyLocked, system, parentStar));

        // Nighttime sky
        weather.setNighttimeSkyDescription(buildNighttimeDescription(
                weather, hasAurora, magField, tidallyLocked));

        // Sunset
        weather.setSunsetDescription(buildSunsetDescription(
                skyColor, atmClass, weather.getTwilightDurationMinutes(), spectralType));
    }

    private String buildDaytimeDescription(String skyColor, double cloudCoverage,
                                            String spectralType, String atmClass,
                                            boolean tidallyLocked, StarSystem system, Star parentStar) {
        StringBuilder desc = new StringBuilder();

        // Sky color
        String colorDesc = skyColorToNatural(skyColor);
        desc.append(colorDesc).append(" sky");

        // Star appearance
        String starChar = spectralType.substring(0, 1);
        switch (starChar) {
            case "O": case "B":
                desc.append(" illuminated by an intense blue-white star");
                break;
            case "A":
                desc.append(" under a brilliant white star");
                break;
            case "F":
                desc.append(" lit by a warm yellow-white star");
                break;
            case "G":
                desc.append(" under a familiar yellow star");
                break;
            case "K":
                desc.append(" bathed in warm orange starlight");
                break;
            case "M":
                desc.append(" under a dim red-orange sun that looms large on the horizon");
                break;
        }

        // Cloud description
        if (cloudCoverage > 90) {
            desc.append(". Perpetual overcast obscures the sky");
        } else if (cloudCoverage > 60) {
            desc.append(". Clouds frequently dominate the sky");
        } else if (cloudCoverage > 30) {
            desc.append(" with scattered cloud formations");
        } else if (cloudCoverage < 10) {
            desc.append(". Skies are almost always clear");
        }

        // Binary companion visibility
        if (system != null) {
            Star companion = parentStar.getCompanionStar();
            if (companion != null) {
                double compLuminosity = companion.getSolarLuminosity() != 0 ? companion.getSolarLuminosity() : 0.01;
                if (compLuminosity > 0.001) {
                    desc.append(". A companion star is visible as a ");
                    if (compLuminosity > 0.1) {
                        desc.append("brilliant second sun");
                    } else {
                        desc.append("bright point of light");
                    }
                }
            }
        }

        // Tidal lock note
        if (tidallyLocked) {
            desc.append(". The star hangs fixed in the sky, never setting on the dayside");
        }

        return truncate(desc.toString(), 500);
    }

    private String buildNighttimeDescription(PlanetaryWeather weather, boolean hasAurora,
                                              PlanetaryMagneticField magField, boolean tidallyLocked) {
        StringBuilder desc = new StringBuilder();

        if (tidallyLocked) {
            desc.append("The permanent nightside never sees the star. ");
        }

        // Moon descriptions
        List<MoonSkyAppearance> moonApps = weather.getMoonSkyAppearances();
        if (moonApps != null && !moonApps.isEmpty()) {
            int visibleMoons = moonApps.size();
            if (visibleMoons == 1) {
                MoonSkyAppearance m = moonApps.get(0);
                double brightness = m.getBrightnessRelativeToFullMoon() != null ? m.getBrightnessRelativeToFullMoon() : 1.0;
                if (brightness > 5) {
                    desc.append("A single large moon dominates the night sky, casting bright shadows");
                } else if (brightness > 1) {
                    desc.append("A bright moon illuminates the landscape");
                } else {
                    desc.append("A dim moon hangs in the darkness");
                }
            } else {
                long brightMoons = moonApps.stream()
                        .filter(m -> m.getBrightnessRelativeToFullMoon() != null && m.getBrightnessRelativeToFullMoon() > 0.5)
                        .count();
                if (brightMoons > 2) {
                    desc.append(String.format("%d moons create a complex dance of light and shadow, " +
                            "with multiple phases visible simultaneously", visibleMoons));
                } else {
                    desc.append(String.format("%d moons are visible, creating an alien skyscape", visibleMoons));
                }
            }
        } else {
            desc.append("No moons — the night sky is dominated by stars");
        }

        // Aurora
        if (hasAurora && magField != null) {
            String colors = magField.getAuroralColors() != null ? magField.getAuroralColors() : "green and purple";
            AuroralIntensity intensity = magField.getAuroralIntensity();

            desc.append(". ");
            if (intensity == AuroralIntensity.SPECTACULAR || intensity == AuroralIntensity.BRIGHT) {
                desc.append("Brilliant auroral displays of ").append(colors)
                        .append(" frequently paint the polar skies");
            } else {
                desc.append("Faint auroral glows of ").append(colors)
                        .append(" occasionally appear near the poles");
            }
        }

        return truncate(desc.toString(), 500);
    }

    private String buildSunsetDescription(String skyColor, String atmClass,
                                           Double twilightMinutes, String spectralType) {
        StringBuilder desc = new StringBuilder();

        String starChar = spectralType.substring(0, 1);

        switch (atmClass) {
            case "VENUS_LIKE":
                desc.append("No visible sunset — the opaque cloud deck prevents any view of the star");
                break;
            case "MARS_LIKE":
                desc.append("A blue-tinged sunset as fine dust scatters blue light forward near the horizon, " +
                        "inverting the usual Rayleigh pattern");
                break;
            case "TITAN_LIKE":
                desc.append("The hazy orange sky gradually dims as the star sinks behind layers of " +
                        "hydrocarbon smog, producing a slow bronze twilight");
                break;
            default:
                // Earth-like and others
                if ("M".equals(starChar)) {
                    desc.append("Deep red sunset as the already-crimson star touches the horizon, " +
                            "painting the sky in shades of blood red and amber");
                } else if ("K".equals(starChar)) {
                    desc.append("Warm golden sunset with deep orange tones near the horizon");
                } else if ("F".equals(starChar) || "A".equals(starChar)) {
                    desc.append("Vivid sunset with strong violet and indigo tones from the hot blue-white star");
                } else {
                    desc.append("Familiar sunset with warm orange and red hues as the star dips below the horizon");
                }
                break;
        }

        // Twilight duration
        if (twilightMinutes != null) {
            if (twilightMinutes > 120) {
                desc.append(". Twilight lingers for hours, creating an extended golden hour");
            } else if (twilightMinutes > 60) {
                desc.append(". Long twilight period provides extended dusk");
            } else if (twilightMinutes < 10) {
                desc.append(". Night falls abruptly with almost no twilight");
            }
        }

        return truncate(desc.toString(), 300);
    }

    private String skyColorToNatural(String skyColor) {
        if (skyColor == null) return "A hazy";
        switch (skyColor) {
            case "BLUE": return "A blue";
            case "DEEP_BLUE": return "A deep blue";
            case "ORANGE": return "An orange";
            case "BUTTERSCOTCH": return "A butterscotch";
            case "NEAR_BLACK": return "A near-black";
            case "PALE_BLUE": return "A pale blue";
            case "CYAN": return "A cyan-teal";
            case "YELLOW_GREY": return "A hazy yellow-grey";
            case "PALE_YELLOW": return "A pale yellow";
            case "BROWN_ORANGE": return "A brownish-orange";
            case "GREEN": return "A greenish";
            case "RED_ORANGE": return "A red-orange";
            case "BLACK": return "A black";
            default: return "A " + skyColor.toLowerCase().replace('_', '-');
        }
    }

    // ================================================================
    // MOON-PERSPECTIVE SKY CALCULATIONS
    // ================================================================

    /**
     * Calculates sky appearance from a moon's surface:
     * - Parent planet dominates the sky (angular size, brightness, phases)
     * - Sibling moons are visible as moving lights
     * - Parent planet can eclipse the star
     */
    public void calculateForMoon(PlanetaryWeather weather, Moon moon, Planet parentPlanet,
                                  Star parentStar, StarSystem system, List<Moon> siblingMoons) {
        // Parent planet visibility from the moon's surface
        if (parentPlanet != null) {
            calculateParentPlanetVisibility(weather, moon, parentPlanet, parentStar);
            calculatePlanetaryEclipses(weather, moon, parentPlanet, parentStar);
        }

        // Sibling moon visibility from this moon's surface
        if (siblingMoons != null && siblingMoons.size() > 1) {
            calculateSiblingMoonVisibility(weather, moon, siblingMoons, parentPlanet, parentStar);
        }

        // Sky descriptions for a moon surface
        generateMoonSurfaceSkyDescriptions(weather, moon, parentPlanet, parentStar, system, siblingMoons);
    }

    private void calculateParentPlanetVisibility(PlanetaryWeather weather, Moon moon,
                                                   Planet parentPlanet, Star parentStar) {
        Double moonOrbitKm = moon.getSemiMajorAxisKm();
        Double planetRadiusEarth = parentPlanet.getEarthRadius();
        if (moonOrbitKm == null || moonOrbitKm <= 0 || planetRadiusEarth == null) return;

        double planetRadiusKm = planetRadiusEarth * EARTH_RADIUS_KM;

        // Angular diameter of the parent planet from the moon's surface
        double angularDiameterRad = 2.0 * Math.atan(planetRadiusKm / moonOrbitKm);
        double angularDiameterDeg = Math.toDegrees(angularDiameterRad);

        // The parent planet appears in the MoonSkyAppearance list with a special marker
        MoonSkyAppearance planetAppearance = new MoonSkyAppearance();
        planetAppearance.setMoonId(null); // Not a moon — it's the parent planet

        planetAppearance.setAngularDiameterDegrees(round4(angularDiameterDeg));

        // Phase cycle: the moon's own orbital period determines how the planet's phase changes
        // For tidally locked moons, the planet is always in roughly the same spot
        // but its illumination still cycles with the orbital period
        Double orbitalPeriodDays = moon.getOrbitalPeriodDays();
        if (orbitalPeriodDays != null) {
            planetAppearance.setPhaseCycleDays(round2(orbitalPeriodDays));
        }

        // Brightness relative to Earth's full Moon
        double planetAlbedo = parentPlanet.getAlbedo() != null ? parentPlanet.getAlbedo() : 0.3;
        double planetDistAU = parentPlanet.getSemiMajorAxisAU() != null ? parentPlanet.getSemiMajorAxisAU() : 1.0;
        double starLuminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;
        double stellarFlux = starLuminosity / (planetDistAU * planetDistAU);

        double brightness = (planetAlbedo / 0.12)
                * Math.pow(planetRadiusKm / 1737.0, 2.0)
                * (stellarFlux / 1.0)
                / Math.pow(moonOrbitKm / 384400.0, 2.0);
        planetAppearance.setBrightnessRelativeToFullMoon(round2(brightness));

        planetAppearance.setIsVisibleInDaytime(true); // Parent planet is always visible in daytime

        // Planet color based on atmosphere type
        String atmClass = parentPlanet.getAtmosphereClassification();
        planetAppearance.setApparentColor(estimatePlanetColor(atmClass, parentPlanet.getAlbedo()));

        // Description
        planetAppearance.setDescription(describeParentPlanetAppearance(
                angularDiameterDeg, brightness, Boolean.TRUE.equals(moon.getTidallyLocked()), atmClass));

        List<MoonSkyAppearance> appearances = weather.getMoonSkyAppearances();
        if (appearances == null) {
            appearances = new ArrayList<>();
        }
        // Insert the parent planet at the front (it's the dominant sky object)
        appearances.add(0, planetAppearance);
        weather.setMoonSkyAppearances(appearances);
    }

    private String estimatePlanetColor(String atmClass, Double albedo) {
        if (atmClass == null) return "Grey";
        return switch (atmClass) {
            case "JOVIAN" -> "Banded amber and white";
            case "ICE_GIANT" -> "Blue-green";
            case "VENUS_LIKE" -> "Brilliant white-yellow";
            case "EARTH_LIKE" -> "Blue-white marbled";
            case "MARS_LIKE" -> "Rusty orange";
            case "TITAN_LIKE" -> "Hazy orange";
            default -> albedo != null && albedo > 0.4 ? "Bright white" : "Dark grey";
        };
    }

    private String describeParentPlanetAppearance(double angularDeg, double brightness,
                                                    boolean tidallyLocked, String atmClass) {
        StringBuilder desc = new StringBuilder();

        double sizeRatio = angularDeg / EARTH_MOON_ANGULAR_DIAMETER_DEG;
        if (sizeRatio > 50) {
            desc.append("The parent planet fills ").append(String.format("%.0f°", angularDeg))
                    .append(" of sky — an overwhelming presence");
        } else if (sizeRatio > 10) {
            desc.append("The parent planet looms large at ")
                    .append(String.format("%.1fx", sizeRatio))
                    .append(" the apparent size of Earth's Moon");
        } else if (sizeRatio > 2) {
            desc.append("The parent planet appears ").append(String.format("%.1fx", sizeRatio))
                    .append(" larger than Earth's Moon");
        } else {
            desc.append("The parent planet appears as a bright disk");
        }

        if (tidallyLocked) {
            desc.append(". Tidally locked — the planet hangs motionless in the sky, " +
                    "its phase slowly cycling through the orbital period");
        }

        if (brightness > 100) {
            desc.append(". Brilliant enough to illuminate the landscape like perpetual twilight");
        } else if (brightness > 20) {
            desc.append(". Casts strong shadows during the moon's nighttime");
        } else if (brightness > 5) {
            desc.append(". Bright enough to read by");
        }

        if ("JOVIAN".equals(atmClass) || "ICE_GIANT".equals(atmClass)) {
            desc.append(". Cloud bands and storm systems are visible to the naked eye");
        }

        return truncate(desc.toString(), 200);
    }

    private static final int MAX_SIBLING_APPEARANCES = 5;
    private static final double MIN_SIBLING_ANGULAR_DIAMETER_DEG = 0.05; // Must be a visible disk, not point-like

    private void calculateSiblingMoonVisibility(PlanetaryWeather weather, Moon targetMoon,
                                                  List<Moon> siblingMoons, Planet parentPlanet,
                                                  Star parentStar) {
        List<MoonSkyAppearance> appearances = weather.getMoonSkyAppearances();
        if (appearances == null) {
            appearances = new ArrayList<>();
        }

        double targetOrbitKm = targetMoon.getSemiMajorAxisKm() != null ? targetMoon.getSemiMajorAxisKm() : 400000.0;
        double planetDistAU = parentPlanet.getSemiMajorAxisAU() != null ? parentPlanet.getSemiMajorAxisAU() : 1.0;
        double starLuminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;

        // Collect candidate siblings, then keep only the most visually significant
        List<MoonSkyAppearance> candidates = new ArrayList<>();

        for (Moon sibling : siblingMoons) {
            if (sibling == targetMoon) continue;

            Double siblingRadiusEarth = sibling.getEarthRadius();
            Double siblingOrbitKm = sibling.getSemiMajorAxisKm();
            if (siblingRadiusEarth == null || siblingOrbitKm == null || siblingOrbitKm <= 0) continue;

            double siblingRadiusKm = siblingRadiusEarth * EARTH_RADIUS_KM;

            // Distance between the two moons varies throughout their orbits.
            // Use orbital separation as a rough average, with a realistic floor:
            // two moons can't orbit closer together than ~3x the larger one's Hill sphere
            double interMoonDistKm = Math.abs(siblingOrbitKm - targetOrbitKm);
            double minSeparation = Math.max(siblingRadiusKm, targetMoon.getEarthRadius() != null
                    ? targetMoon.getEarthRadius() * EARTH_RADIUS_KM : 1000.0) * 10.0;
            interMoonDistKm = Math.max(interMoonDistKm, minSeparation);

            // Angular diameter at typical separation
            double angularDiameterRad = 2.0 * Math.atan(siblingRadiusKm / interMoonDistKm);
            double angularDiameterDeg = Math.toDegrees(angularDiameterRad);

            // Skip point-like siblings — they add no meaningful visual data
            if (angularDiameterDeg < MIN_SIBLING_ANGULAR_DIAMETER_DEG) continue;

            MoonSkyAppearance siblingAppearance = new MoonSkyAppearance();
            // moonId will be null until DB persistence is implemented;
            // siblings are distinguished from the parent planet entry (index 0, moonId=null)
            // by their position in the list (index > 0)
            siblingAppearance.setMoonId(sibling.getId());

            siblingAppearance.setAngularDiameterDegrees(round4(angularDiameterDeg));

            // Phase cycle: synodic period between the two moons
            Double siblingPeriodDays = sibling.getOrbitalPeriodDays();
            Double targetPeriodDays = targetMoon.getOrbitalPeriodDays();
            if (siblingPeriodDays != null && targetPeriodDays != null
                    && Math.abs(siblingPeriodDays - targetPeriodDays) > 0.01) {
                double synodicDays = 1.0 / Math.abs(1.0 / siblingPeriodDays - 1.0 / targetPeriodDays);
                siblingAppearance.setPhaseCycleDays(round2(synodicDays));
            } else if (siblingPeriodDays != null) {
                siblingAppearance.setPhaseCycleDays(round2(siblingPeriodDays));
            }

            // Brightness
            double albedo = sibling.getAlbedo() != null ? sibling.getAlbedo() : 0.15;
            double stellarFlux = starLuminosity / (planetDistAU * planetDistAU);
            double brightness = (albedo / 0.12)
                    * Math.pow(siblingRadiusKm / 1737.0, 2.0)
                    * (stellarFlux / 1.0)
                    / Math.pow(interMoonDistKm / 384400.0, 2.0);
            siblingAppearance.setBrightnessRelativeToFullMoon(round2(brightness));

            siblingAppearance.setIsVisibleInDaytime(angularDiameterDeg > 0.1 && brightness > 0.3);
            siblingAppearance.setApparentColor(estimateMoonColor(albedo));
            siblingAppearance.setDescription(describeMoonAppearance(angularDiameterDeg, brightness, siblingPeriodDays));

            candidates.add(siblingAppearance);
        }

        // Keep only the most visually prominent siblings (sorted by brightness descending)
        candidates.sort((a, b) -> Double.compare(
                b.getBrightnessRelativeToFullMoon() != null ? b.getBrightnessRelativeToFullMoon() : 0,
                a.getBrightnessRelativeToFullMoon() != null ? a.getBrightnessRelativeToFullMoon() : 0));

        int limit = Math.min(candidates.size(), MAX_SIBLING_APPEARANCES);
        for (int i = 0; i < limit; i++) {
            appearances.add(candidates.get(i));
        }

        weather.setMoonSkyAppearances(appearances);
    }

    private void calculatePlanetaryEclipses(PlanetaryWeather weather, Moon moon,
                                              Planet parentPlanet, Star parentStar) {
        List<EclipseData> eclipses = weather.getEclipseData();
        if (eclipses == null) {
            eclipses = new ArrayList<>();
        }

        Double moonOrbitKm = moon.getSemiMajorAxisKm();
        Double planetRadiusEarth = parentPlanet.getEarthRadius();
        if (moonOrbitKm == null || planetRadiusEarth == null) return;

        double planetRadiusKm = planetRadiusEarth * EARTH_RADIUS_KM;
        double planetDistAU = parentPlanet.getSemiMajorAxisAU() != null ? parentPlanet.getSemiMajorAxisAU() : 1.0;
        double starRadiusKm = parentStar.getSolarRadius() * SOLAR_RADIUS_KM;
        double starDistKm = planetDistAU * AU_TO_KM;

        // Star angular diameter from the moon's surface (approximately same as from planet)
        double starAngularDeg = Math.toDegrees(2.0 * Math.atan(starRadiusKm / starDistKm));

        // Parent planet angular diameter from the moon
        double planetAngularDeg = Math.toDegrees(2.0 * Math.atan(planetRadiusKm / moonOrbitKm));

        // The parent planet can eclipse the star — and since the planet is typically
        // MUCH larger than the star's angular size, these are dramatic total eclipses
        if (planetAngularDeg > starAngularDeg * 0.3) {
            String eclipseType;
            if (planetAngularDeg >= starAngularDeg * 1.05) {
                eclipseType = "TOTAL";
            } else if (planetAngularDeg >= starAngularDeg * 0.9) {
                eclipseType = RandomUtils.flipCoin() == 1 ? "TOTAL" : "ANNULAR";
            } else {
                eclipseType = "ANNULAR";
            }

            // Eclipse frequency: once per orbit when orbital planes align
            double moonPeriodDays = moon.getOrbitalPeriodDays() != null ? moon.getOrbitalPeriodDays() : 27.3;
            double inclinationDeg = moon.getOrbitalInclinationDegrees() != null
                    ? moon.getOrbitalInclinationDegrees() : 0.0;

            // Low inclination → eclipse nearly every orbit
            // High inclination → rare eclipses
            double orbitalPeriodsPerYear = 365.25 / moonPeriodDays;
            double eclipseProbability;
            if (inclinationDeg < 2.0) {
                eclipseProbability = RandomUtils.rollRange(0.8, 1.0);
            } else if (inclinationDeg < 10.0) {
                eclipseProbability = RandomUtils.rollRange(0.3, 0.6);
            } else {
                eclipseProbability = RandomUtils.rollRange(0.05, 0.2);
            }
            double frequencyPerYear = orbitalPeriodsPerYear * eclipseProbability;
            frequencyPerYear = Math.max(0.1, Math.min(200.0, frequencyPerYear));

            // Duration: planet shadow crossing time
            // The moon must pass through the planet's shadow cone
            double moonAngularSpeedDegPerMin = 360.0 / (moonPeriodDays * 24.0 * 60.0);
            // Shadow angular width ≈ planet angular diameter (simplified)
            double durationMinutes = planetAngularDeg / Math.max(0.0001, moonAngularSpeedDegPerMin);
            durationMinutes = Math.max(1.0, Math.min(600.0, durationMinutes));

            EclipseData eclipse = new EclipseData();
            eclipse.setEclipseSource("PLANET_SOLAR");
            eclipse.setSourceBodyName(parentPlanet.getName() != null ? parentPlanet.getName() : "Parent Planet");
            eclipse.setEclipseType(eclipseType);
            eclipse.setFrequencyPerYear(round2(frequencyPerYear));
            eclipse.setTypicalDurationMinutes(round2(durationMinutes));

            if (planetAngularDeg > starAngularDeg * 5) {
                eclipse.setDescription("The parent planet's vast disk completely eclipses the star, " +
                        "plunging the moon into an extended darkness as it passes through the planet's shadow");
            } else if ("TOTAL".equals(eclipseType)) {
                eclipse.setDescription("The parent planet fully occults the star, " +
                        "creating periodic total eclipses with each orbit");
            } else {
                eclipse.setDescription("The parent planet partially blocks the star, " +
                        "dimming sunlight during orbital passages behind the planet");
            }

            eclipses.add(eclipse);
        }

        weather.setEclipseData(eclipses);
    }

    private void generateMoonSurfaceSkyDescriptions(PlanetaryWeather weather, Moon moon,
                                                      Planet parentPlanet, Star parentStar,
                                                      StarSystem system, List<Moon> siblingMoons) {
        String atmClass = moon.getAtmosphere() != null ? moon.getAtmosphere().getClassification() : "THIN";
        String skyColor = weather.getSkyColor() != null ? weather.getSkyColor() : "NEAR_BLACK";
        double cloudCoverage = weather.getCloudCoveragePercent() != null ? weather.getCloudCoveragePercent() : 0.0;
        String spectralType = parentStar.getSpectralType() != null ? parentStar.getSpectralType() : "G";
        boolean tidallyLocked = Boolean.TRUE.equals(moon.getTidallyLocked());

        PlanetaryMagneticField magField = moon.getMagneticField();
        boolean hasAurora = magField != null && magField.getAuroralFrequency() != null
                && magField.getAuroralFrequency() != AuroralFrequency.RARE;

        // Daytime sky
        weather.setDaytimeSkyDescription(buildMoonDaytimeDescription(
                skyColor, cloudCoverage, spectralType, tidallyLocked, parentPlanet, siblingMoons));

        // Nighttime sky
        weather.setNighttimeSkyDescription(buildMoonNighttimeDescription(
                weather, hasAurora, magField, tidallyLocked, parentPlanet));

        // Sunset
        weather.setSunsetDescription(buildSunsetDescription(
                skyColor, atmClass, weather.getTwilightDurationMinutes(), spectralType));
    }

    private String buildMoonDaytimeDescription(String skyColor, double cloudCoverage,
                                                 String spectralType, boolean tidallyLocked,
                                                 Planet parentPlanet, List<Moon> siblingMoons) {
        StringBuilder desc = new StringBuilder();

        String colorDesc = skyColorToNatural(skyColor);
        desc.append(colorDesc).append(" sky");

        // Star appearance
        String starChar = spectralType.substring(0, 1);
        switch (starChar) {
            case "O": case "B":
                desc.append(" illuminated by an intense blue-white star");
                break;
            case "A":
                desc.append(" under a brilliant white star");
                break;
            case "F":
                desc.append(" lit by a warm yellow-white star");
                break;
            case "G":
                desc.append(" under a familiar yellow star");
                break;
            case "K":
                desc.append(" bathed in warm orange starlight");
                break;
            case "M":
                desc.append(" under a dim red-orange sun");
                break;
        }

        // Parent planet in the sky
        if (parentPlanet != null) {
            Double planetRadiusEarth = parentPlanet.getEarthRadius();
            Double moonOrbitKm = parentPlanet.getMoons() != null ? null : null; // handled via weather data
            if (planetRadiusEarth != null && planetRadiusEarth > 5) {
                desc.append(". The parent gas giant dominates the horizon");
            } else if (planetRadiusEarth != null && planetRadiusEarth > 1) {
                desc.append(". The parent planet is a prominent disk in the sky");
            }

            if (tidallyLocked) {
                desc.append(", fixed in position but slowly cycling through phases");
            }
        }

        // Sibling moons
        if (siblingMoons != null) {
            long visibleSiblings = siblingMoons.stream()
                    .filter(m -> m.getEarthRadius() != null && m.getEarthRadius() > 0.01)
                    .count() - 1; // Subtract self
            if (visibleSiblings > 5) {
                desc.append(String.format(". %d sibling moons trace paths across the sky", visibleSiblings));
            } else if (visibleSiblings > 1) {
                desc.append(". Sibling moons are visible as moving points of light");
            }
        }

        // Cloud coverage
        if (cloudCoverage > 80) {
            desc.append(". Heavy cloud cover obscures the view");
        } else if (cloudCoverage > 40) {
            desc.append(" with scattered clouds");
        }

        return truncate(desc.toString(), 500);
    }

    private String buildMoonNighttimeDescription(PlanetaryWeather weather, boolean hasAurora,
                                                   PlanetaryMagneticField magField,
                                                   boolean tidallyLocked, Planet parentPlanet) {
        StringBuilder desc = new StringBuilder();

        if (tidallyLocked && parentPlanet != null) {
            // The sub-planetary hemisphere always sees the planet
            desc.append("On the planet-facing hemisphere, the parent planet provides constant illumination");

            List<MoonSkyAppearance> apps = weather.getMoonSkyAppearances();
            if (apps != null && !apps.isEmpty()) {
                MoonSkyAppearance planetApp = apps.get(0); // Parent planet is first
                if (planetApp.getMoonId() == null) { // Confirm it's the planet entry
                    double brightness = planetApp.getBrightnessRelativeToFullMoon() != null
                            ? planetApp.getBrightnessRelativeToFullMoon() : 1.0;
                    if (brightness > 50) {
                        desc.append(", bright enough to create perpetual twilight on the near side");
                    } else if (brightness > 10) {
                        desc.append(", casting strong shadows across the landscape");
                    }
                }
            }

            desc.append(". The anti-planetary hemisphere experiences true darkness");
        } else if (parentPlanet != null) {
            desc.append("The parent planet rises and sets, providing periods of bright nighttime illumination");
        } else {
            desc.append("No parent planet — the night sky is dominated by stars");
        }

        // Sibling moon lights (index 0 is parent planet, rest are siblings)
        List<MoonSkyAppearance> apps = weather.getMoonSkyAppearances();
        if (apps != null && apps.size() > 1) {
            long siblingCount = apps.subList(1, apps.size()).stream()
                    .filter(a -> a.getBrightnessRelativeToFullMoon() != null && a.getBrightnessRelativeToFullMoon() > 0.3)
                    .count();
            if (siblingCount > 3) {
                desc.append(String.format(". %d bright sibling moons create a shifting constellation of lights", siblingCount));
            } else if (siblingCount > 0) {
                desc.append(". Sibling moons add moving lights to the night sky");
            }
        }

        // Aurora
        if (hasAurora && magField != null) {
            String colors = magField.getAuroralColors() != null ? magField.getAuroralColors() : "green and purple";
            AuroralIntensity intensity = magField.getAuroralIntensity();

            desc.append(". ");
            if (intensity == AuroralIntensity.SPECTACULAR || intensity == AuroralIntensity.BRIGHT) {
                desc.append("Brilliant auroral displays of ").append(colors)
                        .append(" paint the polar skies");
            } else {
                desc.append("Faint auroral glows of ").append(colors)
                        .append(" occasionally appear near the poles");
            }
        }

        return truncate(desc.toString(), 500);
    }

    // ================================================================
    // GAS GIANT SKY DESCRIPTION
    // ================================================================

    private void generateGasGiantSkyDescription(PlanetaryWeather weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();

        if ("ICE_GIANT".equals(atmClass)) {
            weather.setDaytimeSkyDescription(
                    "Deep within the hydrogen-methane atmosphere, blue-green light filters through " +
                    "ice crystal clouds. The star appears as a distant bright point above cloud decks");
            weather.setNighttimeSkyDescription(
                    "The nightside glows faintly from internal heat. Methane ice crystals catch " +
                    "occasional flashes of massive lightning deep below");
            weather.setSunsetDescription(
                    "No horizon exists — the atmosphere fades gradually from illuminated cloud tops " +
                    "into the deep interior darkness");
        } else {
            weather.setDaytimeSkyDescription(
                    "Looking up from the cloud tops, the sky is a pale blue hydrogen haze. " +
                    "Towering ammonia thunderheads rise tens of kilometers above the main cloud deck");
            weather.setNighttimeSkyDescription(
                    "The nightside atmosphere glows from internal heat. Massive lightning storms " +
                    "illuminate cloud banks from within like distant artillery");
            weather.setSunsetDescription(
                    "The star's light refracts through hundreds of kilometers of atmosphere, " +
                    "creating prismatic bands of color at the terminator");
        }
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private String truncate(String s, int maxLength) {
        if (s == null) return null;
        return s.length() <= maxLength ? s : s.substring(0, maxLength - 3) + "...";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
