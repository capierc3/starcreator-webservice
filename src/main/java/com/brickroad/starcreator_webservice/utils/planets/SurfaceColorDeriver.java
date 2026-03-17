package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.model.climate.SurfaceDeposit;

import java.util.List;

/**
 * Derives surface colors for a planet based on its physical, compositional,
 * and atmospheric properties.
 * <p>
 * Produces two hex color strings:
 * <ul>
 *   <li><b>Primary</b> — dominant surface material color (rock, ice, ocean, cloud tops)</li>
 *   <li><b>Secondary</b> — accent color (ice caps, volcanic glow, atmospheric haze, terrain variation)</li>
 * </ul>
 * <p>
 * The derivation considers:
 * <ol>
 *   <li>Composition classification (silicate, iron, carbon, ice, ocean, gas, molten, exotic)</li>
 *   <li>Surface temperature (hot → redder, cold → bluer/whiter)</li>
 *   <li>Water and ice coverage (shifts toward blue/white)</li>
 *   <li>Atmosphere classification (dense atmospheres dominate surface appearance)</li>
 *   <li>Volcanism (lava glow as secondary)</li>
 *   <li>Planet type fallback (when composition data is unavailable)</li>
 * </ol>
 * <p>
 * All methods are pure functions — no randomness, no side effects.
 */
public final class SurfaceColorDeriver {

    private SurfaceColorDeriver() {} // Prevent instantiation

    /**
     * Result container for the two derived surface colors.
     */
    public record SurfaceColors(String primary, String secondary) {}

    /**
     * Derive surface colors from all available planet properties.
     *
     * @param planet fully populated planet (after composition, atmosphere, water, terrain)
     * @return primary and secondary hex color strings (e.g., "#88aa66")
     */
    public static SurfaceColors derive(Planet planet) {
        // Priority 1: Planet type overrides for distinctive appearances
        String planetType = planet.getPlanetType();
        if (planetType != null) {
            SurfaceColors typeOverride = applyPlanetTypeOverrides(planetType, planet);
            if (typeOverride != null) {
                return typeOverride;
            }
        }

        // Priority 2: Gas giants and ice giants — atmosphere dominates appearance
        String atmoClass = planet.getAtmosphereClassification();
        if (isGaseous(atmoClass)) {
            return deriveGaseousColors(planet, atmoClass);
        }

        // Priority 3: Rocky/icy bodies — composition is the primary driver
        String primary = deriveFromComposition(planet.getCompositionClassification(), planet);
        String secondary = deriveSecondaryColor(planet, primary);

        return new SurfaceColors(primary, secondary);
    }

    // =========================================================================
    // Gaseous planet colors (atmosphere-dominated)
    // =========================================================================

    private static SurfaceColors deriveGaseousColors(Planet planet, String atmoClass) {
        Double temp = planet.getSurfaceTemp();
        double t = temp != null ? temp : 150.0;

        return switch (atmoClass != null ? atmoClass : "") {
            case "JOVIAN" -> {
                // Jupiter-like: warm tones (ammonia clouds) — bands of ochre, cream, rust
                if (t > 1000) {
                    // Hot Jupiter: glowing clouds
                    yield new SurfaceColors("#d4764e", "#ff6633");
                } else if (t > 200) {
                    // Warm: classic Jupiter bands
                    yield new SurfaceColors("#c4956a", "#d4a574");
                } else {
                    // Cold: muted, paler bands
                    yield new SurfaceColors("#b8a88a", "#9a8a6e");
                }
            }
            case "ICE_GIANT" -> {
                // Uranus/Neptune-like: methane absorption → cyan to deep blue
                if (t > 200) {
                    yield new SurfaceColors("#5588bb", "#4477aa");
                } else {
                    yield new SurfaceColors("#4499bb", "#3388aa");
                }
            }
            default -> {
                // Sub-Neptune / Mini-Neptune / other gas envelope
                yield new SurfaceColors("#6699bb", "#5588aa");
            }
        };
    }

    private static boolean isGaseous(String atmoClass) {
        return "JOVIAN".equals(atmoClass) || "ICE_GIANT".equals(atmoClass);
    }

    // =========================================================================
    // Composition-based primary color
    // =========================================================================

    private static String deriveFromComposition(String compClass, Planet planet) {
        Double temp = planet.getSurfaceTemp();
        double t = temp != null ? temp : 288.0;
        Double waterPct = planet.getLiquidCoveragePercent();
        double water = waterPct != null ? waterPct : 0.0;
        Double icePct = planet.getWaterIceCoveragePercent();
        double ice = icePct != null ? icePct : 0.0;

        if (compClass == null) {
            return fallbackFromTemperature(t);
        }

        return switch (compClass) {
            case "SILICATE_RICH" -> {
                // Earth-like rock: browns, tans, grey-greens depending on temperature
                if (water > 50) {
                    yield liquidDominantColor(planet, "#3377aa");
                } else if (t > 600) {
                    yield "#8b6644"; // Baked brown rock
                } else if (t > 350) {
                    yield "#aa8855"; // Desert tan
                } else if (t > 200) {
                    yield "#88aa66"; // Temperate green-brown (vegetation-possible)
                } else {
                    yield "#99aabb"; // Cold grey-blue rock
                }
            }
            case "IRON_RICH" -> {
                // Mercury-like: dark grey with iron
                if (t > 600) {
                    yield "#777766"; // Hot iron — slight warmth
                } else {
                    yield "#666666"; // Cool dark grey
                }
            }
            case "CARBON_RICH" -> {
                // Dark graphite/diamond surface
                if (t > 1500) {
                    yield "#554433"; // Hot carbon — dark brown glow
                } else {
                    yield "#444444"; // Cold carbon — very dark grey
                }
            }
            case "ICE_RICH" -> {
                // Dominated by water/methane/ammonia ices
                if (t > 200) {
                    yield "#88aacc"; // Warm ice — blue-grey slush
                } else if (t > 100) {
                    yield "#aaccdd"; // Cold ice — light blue
                } else {
                    yield "#ccddee"; // Very cold — bright white-blue
                }
            }
            case "MIXED_SILICATE_ICE" -> {
                // Rocky-icy body (like Pluto, Ganymede)
                if (ice > 50) {
                    yield "#aabbcc"; // Ice-dominated
                } else {
                    yield "#998877"; // Rock-dominated with frost
                }
            }
            case "OCEAN_WORLD" -> {
                // Deep global ocean — color depends on liquid type
                String volatileType = planet.getVolatileType();
                if ("METHANE".equals(volatileType) || "METHANE_ETHANE".equals(volatileType)) {
                    yield t > 100 ? "#886633" : "#775522"; // Amber/brown methane seas
                } else if ("AMMONIA".equals(volatileType) || "AMMONIA_WATER".equals(volatileType)) {
                    yield t > 220 ? "#445588" : "#556699"; // Grey-blue ammonia ocean
                }
                if (t > 350) {
                    yield "#336688"; // Hot ocean — darker blue
                } else if (t > 250) {
                    yield "#3388aa"; // Temperate ocean — vibrant blue
                } else {
                    yield "#4499aa"; // Cool ocean — lighter blue-green
                }
            }
            case "GAS_ENVELOPE" -> {
                // Thick H/He atmosphere over small core (sub-Neptune range)
                yield "#7799bb"; // Hazy blue-grey
            }
            case "MOLTEN_SURFACE" -> {
                // Lava world
                if (t > 2000) {
                    yield "#dd4422"; // Extremely hot — bright lava red
                } else if (t > 1200) {
                    yield "#cc5533"; // Hot — orange-red
                } else {
                    yield "#993322"; // Cooling — dark red-brown
                }
            }
            case "EXOTIC" -> {
                // Unusual chemistry
                if (t > 2000) {
                    yield "#bb6644"; // Ultra-hot exotic
                } else {
                    yield "#887766"; // Cool exotic — neutral brown
                }
            }
            default -> fallbackFromTemperature(t);
        };
    }

    private static String liquidDominantColor(Planet planet, String waterDefault) {
        String volatileType = planet.getVolatileType();
        if ("METHANE".equals(volatileType) || "METHANE_ETHANE".equals(volatileType)) {
            return "#886633"; // Amber/brown methane seas
        }
        if ("AMMONIA".equals(volatileType) || "AMMONIA_WATER".equals(volatileType)) {
            return "#445588"; // Grey-blue ammonia ocean
        }
        return waterDefault;
    }

    private static String fallbackFromTemperature(double temp) {
        if (temp > 1500) return "#cc5533";  // Very hot — lava tones
        if (temp > 600)  return "#aa8855";  // Hot — desert brown
        if (temp > 350)  return "#99aa77";  // Warm — green-tan
        if (temp > 200)  return "#88aa88";  // Temperate — green
        if (temp > 100)  return "#99aabb";  // Cool — grey-blue
        return "#bbccdd";                    // Cold — icy blue
    }

    // =========================================================================
    // Secondary color (accent features)
    // =========================================================================

    private static String deriveSecondaryColor(Planet planet, String primaryColor) {
        Double temp = planet.getSurfaceTemp();
        double t = temp != null ? temp : 288.0;
        Double waterPct = planet.getLiquidCoveragePercent();
        double water = waterPct != null ? waterPct : 0.0;
        Double icePct = planet.getWaterIceCoveragePercent();
        double ice = icePct != null ? icePct : 0.0;
        Boolean volcanic = planet.getHasVolcanicActivity();
        String volcType = planet.getVolcanismType();

        // Priority 1: Active volcanism → lava glow accent
        if (Boolean.TRUE.equals(volcanic) && volcType != null) {
            if (volcType.contains("SILICATE") || volcType.contains("BASALTIC")) {
                return "#cc4422"; // Silicate lava — orange-red
            }
            if (volcType.contains("CRYOVOLCANIC") || volcType.contains("CRYO")) {
                return "#aaddee"; // Cryovolcanism — icy blue-white
            }
            if (volcType.contains("SULFUR")) {
                return "#ccaa33"; // Sulfur volcanism — yellow (Io-like)
            }
            return "#bb5533"; // Generic volcanism glow
        }

        // Priority 2: Significant ice → white/light blue polar caps
        if (ice > 30) {
            return "#ddeeff"; // Ice cap white-blue
        }

        // Priority 3: Significant liquid → accent color matches liquid type
        if (water > 30) {
            String liqColor = planet.getLiquidColorPrimary();
            return liqColor != null ? liqColor : "#4488bb";
        }

        // Priority 4: Temperature-based accent
        if (t > 1500) {
            return "#ff6633"; // Extreme heat glow
        }
        if (t > 600) {
            return "#cc9955"; // Hot — sandy accent
        }
        if (t < 100) {
            return "#ccddee"; // Very cold — frost accent
        }

        // Default: slightly different shade of primary (terrain variation)
        return shiftColor(primaryColor, 15);
    }

    // =========================================================================
    // Planet type overrides (distinctive appearances)
    // =========================================================================

    private static SurfaceColors applyPlanetTypeOverrides(String planetType, Planet planet) {
        String t = planetType.toLowerCase();
        Double temp = planet.getSurfaceTemp();

        // Lava Planet: always dominated by molten surface
        if (t.contains("lava")) {
            double surfTemp = temp != null ? temp : 1500.0;
            if (surfTemp > 2000) {
                return new SurfaceColors("#dd4422", "#ff6633");
            }
            return new SurfaceColors("#cc5533", "#ee5544");
        }

        // Ocean Planet: color depends on liquid type
        if (t.contains("ocean")) {
            Double icePct = planet.getWaterIceCoveragePercent();
            if (icePct != null && icePct > 40) {
                return new SurfaceColors("#4499aa", "#ddeeff"); // Frozen ocean
            }
            String volatileType = planet.getVolatileType();
            if ("METHANE".equals(volatileType) || "METHANE_ETHANE".equals(volatileType)) {
                return new SurfaceColors("#886633", "#997744");
            } else if ("AMMONIA".equals(volatileType) || "AMMONIA_WATER".equals(volatileType)) {
                return new SurfaceColors("#445588", "#556699");
            }
            return new SurfaceColors("#3388aa", "#55aacc");
        }

        // Desert Planet: sandy tones
        if (t.contains("desert")) {
            return new SurfaceColors("#cc9955", "#bb8844");
        }

        // Ice World: bright icy surface
        if (t.contains("ice world")) {
            return new SurfaceColors("#bbccdd", "#ddeeff");
        }

        // Iron Planet: dark metallic grey
        if (t.contains("iron")) {
            double surfTemp = temp != null ? temp : 400.0;
            if (surfTemp > 800) {
                return new SurfaceColors("#777766", "#666655");
            }
            return new SurfaceColors("#666666", "#555555");
        }

        // Carbon Planet: very dark
        if (t.contains("carbon")) {
            return new SurfaceColors("#444444", "#333333");
        }

        // Hot Jupiter: exotic cloud colors
        if (t.contains("hot jupiter")) {
            return new SurfaceColors("#d4764e", "#cc6644");
        }

        // Puffy Planet: inflated hot gas giant
        if (t.contains("puffy")) {
            return new SurfaceColors("#dd8855", "#cc7744");
        }

        // Dwarf Planet: small, icy/rocky, muted
        if (t.contains("dwarf")) {
            return new SurfaceColors("#8899aa", "#778899");
        }

        // No override needed — composition-derived colors are adequate
        return null;
    }

    // =========================================================================
    // Deposit-based color adjustment
    // =========================================================================

    /**
     * Adjust a planet or moon's surface colors based on dominant surface deposits.
     * Called after climate generation, so deposits are available.
     * <p>
     * If the dominant deposit covers >30% of the surface, the primary color
     * blends toward the deposit's color hint. If a second deposit covers >15%,
     * the secondary color blends toward that deposit's color.
     *
     * @param currentPrimary   current surface primary color hex
     * @param currentSecondary current surface secondary color hex
     * @param deposits         computed surface deposits (sorted by dominance)
     * @return adjusted colors, or original if no significant deposits
     */
    public static SurfaceColors adjustForDeposits(String currentPrimary, String currentSecondary,
                                                   List<SurfaceDeposit> deposits) {
        if (deposits == null || deposits.isEmpty()) {
            return new SurfaceColors(currentPrimary, currentSecondary);
        }

        String primary = currentPrimary != null ? currentPrimary : "#888888";
        String secondary = currentSecondary != null ? currentSecondary : "#777777";

        // Dominant deposit influences primary color
        SurfaceDeposit dominant = deposits.get(0);
        if (dominant.getCoveragePercent() != null && dominant.getCoveragePercent() > 30
                && dominant.getColorHint() != null) {
            double weight = Math.min(0.7, dominant.getCoveragePercent() / 100.0);
            primary = blendColors(primary, dominant.getColorHint(), weight);
        }

        // Second deposit influences secondary color
        if (deposits.size() > 1) {
            SurfaceDeposit second = deposits.get(1);
            if (second.getCoveragePercent() != null && second.getCoveragePercent() > 15
                    && second.getColorHint() != null) {
                double weight = Math.min(0.5, second.getCoveragePercent() / 100.0);
                secondary = blendColors(secondary, second.getColorHint(), weight);
            }
        }

        return new SurfaceColors(primary, secondary);
    }

    /**
     * Blend two hex colors with a weight (0 = all colorA, 1 = all colorB).
     */
    static String blendColors(String colorA, String colorB, double weight) {
        if (colorA == null || colorA.length() != 7) return colorB;
        if (colorB == null || colorB.length() != 7) return colorA;
        try {
            int rA = Integer.parseInt(colorA.substring(1, 3), 16);
            int gA = Integer.parseInt(colorA.substring(3, 5), 16);
            int bA = Integer.parseInt(colorA.substring(5, 7), 16);
            int rB = Integer.parseInt(colorB.substring(1, 3), 16);
            int gB = Integer.parseInt(colorB.substring(3, 5), 16);
            int bB = Integer.parseInt(colorB.substring(5, 7), 16);

            int r = clamp((int) (rA * (1 - weight) + rB * weight));
            int g = clamp((int) (gA * (1 - weight) + gB * weight));
            int b = clamp((int) (bA * (1 - weight) + bB * weight));

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (NumberFormatException e) {
            return colorA;
        }
    }

    // =========================================================================
    // Color utility
    // =========================================================================

    /**
     * Shift a hex color slightly to create a complementary accent.
     * Adds/subtracts a small amount to each RGB channel.
     */
    static String shiftColor(String hex, int amount) {
        if (hex == null || hex.length() != 7) return "#887766";
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            // Shift toward a slightly warmer/brighter variant
            r = clamp(r + amount);
            g = clamp(g - amount / 2);
            b = clamp(b - amount / 3);

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (NumberFormatException e) {
            return "#887766";
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
