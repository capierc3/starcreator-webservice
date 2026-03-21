package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.model.climate.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes orbital light cycle data — how daylight varies by latitude through the orbit.
 * <p>
 * Divides the orbit into four quadrants (two solstices, two equinoxes) and calculates
 * day length, solar elevation, and estimated temperature at sampled latitudes for each.
 */
@Component
public class OrbitalLightCycleCalculator {

    // Standard latitude samples (north to south)
    private static final double[] STANDARD_LATITUDES = {90, 60, 45, 30, 0, -30, -45, -60, -90};

    // Quadrant definitions: index, name, label, midpoint orbital angle (degrees)
    // Prograde rotation (tilt ≤ 90°): north pole sunward at orbital angle 90°
    private static final String[] QUADRANT_NAMES = {
            "NORTH_POLE_SUNWARD", "DESCENDING_EQUINOX", "SOUTH_POLE_SUNWARD", "ASCENDING_EQUINOX"
    };
    private static final String[] QUADRANT_LABELS = {
            "North Pole Facing Star", "Transition to Southern Exposure",
            "South Pole Facing Star", "Transition to Northern Exposure"
    };
    // Retrograde rotation (tilt > 90°): seasons are inverted — south pole sunward at orbital angle 90°
    private static final String[] RETROGRADE_QUADRANT_NAMES = {
            "SOUTH_POLE_SUNWARD", "ASCENDING_EQUINOX", "NORTH_POLE_SUNWARD", "DESCENDING_EQUINOX"
    };
    private static final String[] RETROGRADE_QUADRANT_LABELS = {
            "South Pole Facing Star", "Transition to Northern Exposure",
            "North Pole Facing Star", "Transition to Southern Exposure"
    };
    // Midpoint orbital angles where solar declination is evaluated
    // 90° = max northern declination, 180° = equinox, 270° = max southern, 0° = equinox
    private static final double[] QUADRANT_MIDPOINT_ANGLES = {90, 180, 270, 0};

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryClimate climate, Planet planet) {
        double axialTilt = planet.getAxialTilt() != null ? planet.getAxialTilt() : 0.0;
        double rotationHours = planet.getRotationPeriodHours() != null ? Math.abs(planet.getRotationPeriodHours()) : 24.0;
        double orbitalPeriodDays = planet.getOrbitalPeriodDays() != null ? planet.getOrbitalPeriodDays() : 365.25;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());

        boolean retrograde = axialTilt > 90.0;
        double effectiveTilt = retrograde ? 180.0 - axialTilt : axialTilt;
        double arcticCircle = 90.0 - effectiveTilt;
        double tropicLine = effectiveTilt;

        // Synodic (solar) day: accounts for orbital motion
        double solarDayHours = calculateSolarDay(rotationHours, orbitalPeriodDays);

        OrbitalLightCycle cycle = new OrbitalLightCycle();
        cycle.setEffectiveObliquityDeg(round2(effectiveTilt));
        cycle.setTropicLatitudeDeg(round2(tropicLine));
        cycle.setArcticCircleLatitudeDeg(round2(arcticCircle));
        cycle.setSolarDayHours(round2(solarDayHours));
        cycle.setOrbitalPeriodDays(round2(orbitalPeriodDays));

        // Perpetual daylight coverage: fraction of surface above arctic circle
        // Spherical geometry: coverage = 1 - sin(arcticCircle)
        double perpetualCoverage = (1.0 - Math.sin(Math.toRadians(arcticCircle))) * 100.0;
        cycle.setPerpetualDaylightCoveragePercent(round2(perpetualCoverage));

        // Light cycle class
        String lightCycleClass = classifyLightCycle(effectiveTilt, tidallyLocked,
                planet.getAtmosphereClassification());
        cycle.setLightCycleClass(lightCycleClass);

        if (tidallyLocked) {
            cycle.setLightCycleSummary(
                    "Tidally locked — one hemisphere permanently faces the star while the other is in " +
                            "perpetual darkness. Orbital position does not change the day/night pattern. " +
                            "A narrow twilight band encircles the terminator.");
            cycle.setQuadrants(new ArrayList<>());
            cycle.setLatitudeProfiles(new ArrayList<>());
            climate.setOrbitalLightCycle(cycle);
            return;
        }

        // Build latitude sample list
        List<Double> latitudes = buildLatitudeSamples(arcticCircle, tropicLine);

        // Temperature data from prior climate calculations
        double equatorialTemp = climate.getMeanEquatorialTempK() != null ? climate.getMeanEquatorialTempK() : 250.0;
        double polarTemp = climate.getMeanPolarTempK() != null ? climate.getMeanPolarTempK() : 200.0;
        double seasonalAmp = climate.getSeasonalTempAmplitudeK() != null ? climate.getSeasonalTempAmplitudeK() : 0.0;
        double dayNightRange = climate.getDayNightTempRangeK() != null ? climate.getDayNightTempRangeK() : 10.0;

        // Build quadrants
        double quadrantDuration = orbitalPeriodDays / 4.0;
        List<OrbitalQuadrant> quadrants = new ArrayList<>();

        // Select quadrant names/labels based on rotation direction
        String[] quadrantNames = retrograde ? RETROGRADE_QUADRANT_NAMES : QUADRANT_NAMES;
        String[] quadrantLabels = retrograde ? RETROGRADE_QUADRANT_LABELS : QUADRANT_LABELS;

        for (int i = 0; i < 4; i++) {
            OrbitalQuadrant q = new OrbitalQuadrant();
            q.setQuadrantIndex(i);
            q.setQuadrantName(quadrantNames[i]);
            q.setLabel(quadrantLabels[i]);
            q.setOrbitalDayStart(round2(i * quadrantDuration));
            q.setOrbitalDayEnd(round2((i + 1) * quadrantDuration));
            q.setDurationDays(round2(quadrantDuration));

            // Solar declination at midpoint
            // For retrograde planets (tilt > 90°), negate declination: the rotation-defined
            // north pole points opposite to the orbital north, so the seasonal cycle inverts.
            double midpointAngleRad = Math.toRadians(QUADRANT_MIDPOINT_ANGLES[i]);
            double solarDeclination = Math.toDegrees(Math.asin(
                    Math.sin(Math.toRadians(effectiveTilt)) * Math.sin(midpointAngleRad)));
            if (retrograde) {
                solarDeclination = -solarDeclination;
            }
            q.setSolarDeclinationAtMidpointDeg(round2(solarDeclination));

            // Compute daylight snapshots for each latitude
            List<LatitudeDaylightSnapshot> snapshots = new ArrayList<>();
            for (double lat : latitudes) {
                snapshots.add(buildSnapshot(lat, solarDeclination, solarDayHours,
                        equatorialTemp, polarTemp, seasonalAmp, dayNightRange, effectiveTilt));
            }
            q.setDaylightByLatitude(snapshots);

            // Quadrant narrative
            q.setDescription(generateQuadrantDescription(i, solarDeclination, effectiveTilt,
                    arcticCircle, quadrantDuration, snapshots));

            quadrants.add(q);
        }
        cycle.setQuadrants(quadrants);

        // Build latitude profiles (annual summary per latitude)
        List<LatitudeDaylightProfile> profiles = new ArrayList<>();
        for (double lat : latitudes) {
            profiles.add(buildLatitudeProfile(lat, quadrants, quadrantDuration, orbitalPeriodDays));
        }
        cycle.setLatitudeProfiles(profiles);

        // Light cycle summary
        cycle.setLightCycleSummary(generateLightCycleSummary(effectiveTilt, arcticCircle,
                perpetualCoverage, solarDayHours, orbitalPeriodDays, lightCycleClass));

        climate.setOrbitalLightCycle(cycle);
    }

    // ================================================================
    // SOLAR DAY CALCULATION
    // ================================================================

    /**
     * Synodic day = time between successive solar noons, accounting for orbital motion.
     * For prograde rotation: solarDay = 1 / (1/siderealDay - 1/orbitalPeriod)
     */
    private double calculateSolarDay(double siderealDayHours, double orbitalPeriodDays) {
        double orbitalPeriodHours = orbitalPeriodDays * 24.0;
        if (Math.abs(siderealDayHours - orbitalPeriodHours) < 0.01) {
            // Effectively tidally locked — infinite solar day
            return orbitalPeriodHours;
        }
        double solarDay = 1.0 / (1.0 / siderealDayHours - 1.0 / orbitalPeriodHours);
        // If negative (retrograde-like edge case), take absolute value
        return Math.abs(solarDay);
    }

    // ================================================================
    // LATITUDE SAMPLING
    // ================================================================

    private List<Double> buildLatitudeSamples(double arcticCircle, double tropicLine) {
        List<Double> samples = new ArrayList<>();

        // Start with standard latitudes
        for (double lat : STANDARD_LATITUDES) {
            samples.add(lat);
        }

        // Dynamically add arctic circle and tropic latitudes if they differ enough from standard samples
        addDynamicLatitude(samples, arcticCircle);
        addDynamicLatitude(samples, -arcticCircle);
        addDynamicLatitude(samples, tropicLine);
        addDynamicLatitude(samples, -tropicLine);

        // Sort north to south (descending)
        samples.sort((a, b) -> Double.compare(b, a));

        // Remove duplicates (within 0.5°)
        List<Double> deduped = new ArrayList<>();
        for (double lat : samples) {
            if (deduped.isEmpty() || Math.abs(deduped.get(deduped.size() - 1) - lat) > 0.5) {
                deduped.add(lat);
            }
        }
        return deduped;
    }

    private void addDynamicLatitude(List<Double> samples, double lat) {
        // Only add if it differs from all existing samples by more than 5°
        for (double existing : samples) {
            if (Math.abs(existing - lat) < 5.0) {
                return;
            }
        }
        samples.add(lat);
    }

    // ================================================================
    // DAYLIGHT SNAPSHOT
    // ================================================================

    private LatitudeDaylightSnapshot buildSnapshot(double latitudeDeg, double solarDeclinationDeg,
                                                    double solarDayHours,
                                                    double equatorialTemp, double polarTemp,
                                                    double seasonalAmp, double dayNightRange,
                                                    double effectiveTilt) {
        LatitudeDaylightSnapshot snap = new LatitudeDaylightSnapshot();
        snap.setLatitudeDeg(latitudeDeg);
        snap.setLatitudeLabel(formatLatitudeLabel(latitudeDeg));

        double latRad = Math.toRadians(latitudeDeg);
        double decRad = Math.toRadians(solarDeclinationDeg);

        // Max solar elevation: 90° - |latitude - declination|
        double maxElevation = 90.0 - Math.abs(latitudeDeg - solarDeclinationDeg);
        snap.setMaxSolarElevationDeg(round2(Math.max(-90.0, Math.min(90.0, maxElevation))));

        // Day length calculation
        // Handle pole edge cases (tan(90°) is undefined)
        if (Math.abs(latitudeDeg) >= 89.99) {
            // At the poles: daylight depends entirely on sign of declination
            boolean northPole = latitudeDeg > 0;
            boolean sunAboveHorizon = northPole ? solarDeclinationDeg > 0 : solarDeclinationDeg < 0;
            boolean sunOnHorizon = Math.abs(solarDeclinationDeg) < 0.5;

            if (sunOnHorizon) {
                // Equinox at pole — sun on the horizon, count as ~half day
                snap.setDaylightHours(round2(solarDayHours / 2.0));
                snap.setPerpetualDaylight(false);
                snap.setPerpetualDarkness(false);
            } else if (sunAboveHorizon) {
                snap.setPerpetualDaylight(true);
                snap.setPerpetualDarkness(false);
                snap.setDaylightHours(null);
            } else {
                snap.setPerpetualDaylight(false);
                snap.setPerpetualDarkness(true);
                snap.setDaylightHours(null);
            }
        } else {
            double tanProduct = Math.tan(latRad) * Math.tan(decRad);

            if (tanProduct > 1.0) {
                // Perpetual daylight
                snap.setPerpetualDaylight(true);
                snap.setPerpetualDarkness(false);
                snap.setDaylightHours(null);
            } else if (tanProduct < -1.0) {
                // Perpetual darkness
                snap.setPerpetualDaylight(false);
                snap.setPerpetualDarkness(true);
                snap.setDaylightHours(null);
            } else {
                double hourAngle = Math.acos(-tanProduct);
                double daylightHours = solarDayHours * hourAngle / Math.PI;
                snap.setDaylightHours(round2(daylightHours));
                snap.setPerpetualDaylight(false);
                snap.setPerpetualDarkness(false);
            }
        }

        // Temperature estimates
        calculateSnapshotTemperatures(snap, latitudeDeg, solarDeclinationDeg, solarDayHours,
                equatorialTemp, polarTemp, seasonalAmp, dayNightRange, effectiveTilt);

        // Description
        snap.setDescription(generateSnapshotDescription(snap, solarDayHours));

        return snap;
    }

    // ================================================================
    // TEMPERATURE PER SNAPSHOT
    // ================================================================

    private void calculateSnapshotTemperatures(LatitudeDaylightSnapshot snap,
                                                double latitudeDeg, double solarDeclinationDeg,
                                                double solarDayHours,
                                                double equatorialTemp, double polarTemp,
                                                double seasonalAmp, double dayNightRange,
                                                double effectiveTilt) {
        // Base temp at this latitude: interpolate using cos(latitude)
        double absLat = Math.abs(latitudeDeg);
        double latFraction = absLat / 90.0;
        double baseTempAtLat = equatorialTemp + (polarTemp - equatorialTemp) * latFraction;

        // Seasonal offset: how much warmer/cooler due to orbital position
        // Positive when this hemisphere is tilted toward the star
        double seasonalOffset = 0.0;
        if (effectiveTilt > 0.5 && seasonalAmp > 0.1) {
            double declinationNormalized = solarDeclinationDeg / effectiveTilt; // -1 to +1
            double latSign = latitudeDeg >= 0 ? 1.0 : -1.0;
            // Seasonal effect is strongest at poles, weakest at equator
            double latitudeWeight = Math.sin(Math.toRadians(absLat));
            seasonalOffset = seasonalAmp * declinationNormalized * latSign * latitudeWeight;
        }

        double adjustedTemp = baseTempAtLat + seasonalOffset;

        // Day/night temperature split
        if (Boolean.TRUE.equals(snap.getPerpetualDaylight())) {
            snap.setMeanDaytimeTempK(round2(adjustedTemp));
            snap.setMeanNighttimeTempK(null);
        } else if (Boolean.TRUE.equals(snap.getPerpetualDarkness())) {
            snap.setMeanDaytimeTempK(null);
            snap.setMeanNighttimeTempK(round2(adjustedTemp));
        } else if (snap.getDaylightHours() != null && solarDayHours > 0) {
            double daylightFraction = snap.getDaylightHours() / solarDayHours;
            daylightFraction = Math.max(0.01, Math.min(0.99, daylightFraction));

            // Scale the day/night range by day length: longer days accumulate more
            // heat (larger daytime boost, smaller nighttime drop) and vice versa.
            // At 50/50 day/night the range is unchanged; extremes amplify it.
            double dayLengthScale = 2.0 * Math.max(daylightFraction, 1.0 - daylightFraction);
            double scaledRange = dayNightRange * dayLengthScale;

            // Distribute: daytime above mean, nighttime below
            double daytimeTemp = adjustedTemp + scaledRange * (1.0 - daylightFraction);
            double nighttimeTemp = adjustedTemp - scaledRange * daylightFraction;
            snap.setMeanDaytimeTempK(round2(daytimeTemp));
            snap.setMeanNighttimeTempK(round2(nighttimeTemp));
        }
    }

    // ================================================================
    // LATITUDE PROFILES
    // ================================================================

    private LatitudeDaylightProfile buildLatitudeProfile(double latitudeDeg,
                                                          List<OrbitalQuadrant> quadrants,
                                                          double quadrantDuration,
                                                          double orbitalPeriodDays) {
        LatitudeDaylightProfile profile = new LatitudeDaylightProfile();
        profile.setLatitudeDeg(latitudeDeg);
        profile.setLatitudeLabel(formatLatitudeLabel(latitudeDeg));

        Double minDaylight = null;
        Double maxDaylight = null;
        boolean hasPerpetualDay = false;
        boolean hasPerpetualNight = false;

        for (OrbitalQuadrant q : quadrants) {
            for (LatitudeDaylightSnapshot snap : q.getDaylightByLatitude()) {
                if (Math.abs(snap.getLatitudeDeg() - latitudeDeg) < 0.1) {
                    if (Boolean.TRUE.equals(snap.getPerpetualDaylight())) {
                        hasPerpetualDay = true;
                    } else if (Boolean.TRUE.equals(snap.getPerpetualDarkness())) {
                        hasPerpetualNight = true;
                    } else if (snap.getDaylightHours() != null) {
                        if (minDaylight == null || snap.getDaylightHours() < minDaylight) {
                            minDaylight = snap.getDaylightHours();
                        }
                        if (maxDaylight == null || snap.getDaylightHours() > maxDaylight) {
                            maxDaylight = snap.getDaylightHours();
                        }
                    }
                    break;
                }
            }
        }

        profile.setExperiencesPerpetualDaylight(hasPerpetualDay);
        profile.setExperiencesPerpetualDarkness(hasPerpetualNight);
        profile.setMinDaylightHours(hasPerpetualNight ? null : minDaylight);
        profile.setMaxDaylightHours(hasPerpetualDay ? null : maxDaylight);

        profile.setAnnualDescription(generateAnnualDescription(latitudeDeg, minDaylight, maxDaylight,
                hasPerpetualDay, hasPerpetualNight, quadrantDuration, orbitalPeriodDays));

        return profile;
    }

    // ================================================================
    // CLASSIFICATION
    // ================================================================

    private String classifyLightCycle(double effectiveTilt, boolean tidallyLocked, String atmClass) {
        if (tidallyLocked) return "TIDALLY_LOCKED";
        if (effectiveTilt < 5.0) return "MINIMAL_TILT";
        if (effectiveTilt < 25.0) return "MODERATE_TILT";
        if (effectiveTilt < 50.0) return "EXTREME_TILT";
        return "NEAR_POLAR_TILT";
    }

    // ================================================================
    // LABELS & FORMATTING
    // ================================================================

    private String formatLatitudeLabel(double latitudeDeg) {
        if (Math.abs(latitudeDeg - 90.0) < 0.5) return "North Pole";
        if (Math.abs(latitudeDeg + 90.0) < 0.5) return "South Pole";
        if (Math.abs(latitudeDeg) < 0.5) return "Equator";

        String dir = latitudeDeg > 0 ? "N" : "S";
        double absLat = Math.abs(latitudeDeg);
        // Format without decimals if it's a whole number
        if (absLat == Math.floor(absLat)) {
            return String.format("%.0f°%s", absLat, dir);
        }
        return String.format("%.1f°%s", absLat, dir);
    }

    // ================================================================
    // NARRATIVE GENERATION
    // ================================================================

    private String generateSnapshotDescription(LatitudeDaylightSnapshot snap, double solarDayHours) {
        if (Boolean.TRUE.equals(snap.getPerpetualDaylight())) {
            double elev = snap.getMaxSolarElevationDeg() != null ? snap.getMaxSolarElevationDeg() : 0;
            if (elev > 70) {
                return "Continuous daylight — sun passes near the zenith";
            } else if (elev > 30) {
                return String.format("Continuous daylight — sun circles the sky up to %.0f° elevation", elev);
            } else {
                return String.format("Continuous daylight — sun circles at low elevation (%.0f° max)", elev);
            }
        }
        if (Boolean.TRUE.equals(snap.getPerpetualDarkness())) {
            return "Perpetual darkness — sun never rises above the horizon";
        }
        if (snap.getDaylightHours() == null) return "";

        double hours = snap.getDaylightHours();
        double fraction = hours / solarDayHours;

        if (fraction > 0.85) {
            return String.format("Very long days — %.1f hours of daylight", hours);
        } else if (fraction > 0.6) {
            return String.format("Extended daylight — %.1f hours", hours);
        } else if (fraction > 0.4) {
            return String.format("Near-equal day and night — %.1f hours of daylight", hours);
        } else if (fraction > 0.15) {
            return String.format("Short days — %.1f hours of daylight", hours);
        } else {
            return String.format("Sun barely rises — only %.1f hours of dim daylight", hours);
        }
    }

    private String generateQuadrantDescription(int index, double solarDeclination,
                                                double effectiveTilt, double arcticCircle,
                                                double quadrantDuration,
                                                List<LatitudeDaylightSnapshot> snapshots) {
        StringBuilder sb = new StringBuilder();

        if (solarDeclination > 1.0) {
            // Positive declination → north pole sunward
            sb.append(String.format("The north pole tilts toward the star (solar declination %.1f°). ", solarDeclination));
            appendPerpetualDaylightSummary(sb, snapshots, true, arcticCircle, quadrantDuration);
        } else if (solarDeclination < -1.0) {
            // Negative declination → south pole sunward
            sb.append(String.format("The south pole tilts toward the star (solar declination %.1f°). ", solarDeclination));
            appendPerpetualDaylightSummary(sb, snapshots, false, arcticCircle, quadrantDuration);
        } else {
            // Equinox
            sb.append("Both poles receive similar illumination. All latitudes experience sunrise and sunset. ");
            sb.append(String.format("Duration: %.0f days.", quadrantDuration));
        }

        return sb.toString();
    }

    private void appendPerpetualDaylightSummary(StringBuilder sb, List<LatitudeDaylightSnapshot> snapshots,
                                                 boolean northSunward, double arcticCircle,
                                                 double quadrantDuration) {
        long perpetualDayCount = snapshots.stream()
                .filter(s -> Boolean.TRUE.equals(s.getPerpetualDaylight())).count();
        long perpetualNightCount = snapshots.stream()
                .filter(s -> Boolean.TRUE.equals(s.getPerpetualDarkness())).count();

        if (arcticCircle < 20) {
            sb.append(String.format("Nearly the entire surface %s of the equator is in continuous daylight ",
                    northSunward ? "north" : "south"));
            sb.append(String.format("while the opposite hemisphere is in total darkness. "));
        } else {
            sb.append(String.format("Regions %s of %.0f° experience continuous daylight. ",
                    northSunward ? "north" : "south", arcticCircle));
        }
        sb.append(String.format("Duration: %.0f days.", quadrantDuration));
    }

    private String generateLightCycleSummary(double effectiveTilt, double arcticCircle,
                                              double perpetualCoverage, double solarDayHours,
                                              double orbitalPeriodDays, String lightCycleClass) {
        StringBuilder sb = new StringBuilder();

        switch (lightCycleClass) {
            case "MINIMAL_TILT" -> {
                sb.append(String.format("Minimal axial tilt (%.1f°) produces nearly uniform daylight year-round. ", effectiveTilt));
                sb.append(String.format("Solar days are %.1f hours. ", solarDayHours));
                sb.append("All latitudes experience similar day lengths throughout the orbit with negligible seasonal variation.");
            }
            case "MODERATE_TILT" -> {
                sb.append(String.format("Moderate axial tilt (%.1f°) produces familiar seasonal patterns. ", effectiveTilt));
                sb.append(String.format("The tropical band extends to %.0f° latitude. ", effectiveTilt));
                sb.append(String.format("Only polar regions above %.0f° experience periods of continuous daylight or darkness. ", arcticCircle));
                sb.append(String.format("%.1f%% of the surface sees perpetual daylight at some point.", perpetualCoverage));
            }
            case "EXTREME_TILT" -> {
                sb.append(String.format("Extreme axial tilt (%.1f°) creates dramatic seasonal shifts. ", effectiveTilt));
                sb.append(String.format("The arctic circle sits at %.0f° latitude — ", arcticCircle));
                sb.append(String.format("%.0f%% of the surface experiences periods of perpetual daylight and darkness. ", perpetualCoverage));
                sb.append("Mid-latitudes see day lengths ranging from a few hours to near-continuous daylight depending on orbital position.");
            }
            case "NEAR_POLAR_TILT" -> {
                sb.append(String.format("Near-polar axial tilt (%.1f°) creates extreme light patterns. ", effectiveTilt));
                sb.append(String.format("The arctic circle sits at just %.0f° latitude — ", arcticCircle));
                sb.append(String.format("%.0f%% of the surface experiences periods of perpetual daylight and perpetual darkness. ", perpetualCoverage));
                sb.append("At solstice, one hemisphere is bathed in continuous starlight while the other is plunged into total darkness. ");
                sb.append(String.format("Only a narrow equatorial band between %.0f°N and %.0f°S always sees sunrise and sunset.",
                        arcticCircle, arcticCircle));
            }
            default -> sb.append("Light cycle data computed.");
        }

        return sb.toString();
    }

    private String generateAnnualDescription(double latitudeDeg, Double minDaylight, Double maxDaylight,
                                              boolean hasPerpetualDay, boolean hasPerpetualNight,
                                              double quadrantDuration, double orbitalPeriodDays) {
        String monthsPerQuadrant = formatDuration(quadrantDuration);

        if (hasPerpetualDay && hasPerpetualNight) {
            if (Math.abs(latitudeDeg) > 85) {
                return String.format("Half the orbit in continuous daylight, half in total darkness — each lasting ~%s",
                        formatDuration(orbitalPeriodDays / 2.0));
            }
            return String.format("Swings from %s of continuous daylight to %s of total darkness with brief transitional periods",
                    monthsPerQuadrant, monthsPerQuadrant);
        }
        if (hasPerpetualDay && minDaylight != null) {
            return String.format("Ranges from %.1f-hour days to periods of continuous daylight lasting ~%s",
                    minDaylight, monthsPerQuadrant);
        }
        if (hasPerpetualNight && maxDaylight != null) {
            return String.format("Ranges from periods of total darkness lasting ~%s to %.1f-hour days",
                    monthsPerQuadrant, maxDaylight);
        }
        if (minDaylight != null && maxDaylight != null) {
            double range = maxDaylight - minDaylight;
            if (range < 1.0) {
                return String.format("Nearly constant %.1f-hour days year-round", (minDaylight + maxDaylight) / 2.0);
            }
            return String.format("Day length ranges from %.1f to %.1f hours through the orbit", minDaylight, maxDaylight);
        }
        return "Day/night pattern varies through the orbit";
    }

    private String formatDuration(double days) {
        if (days > 365) {
            double years = days / 365.25;
            return String.format("%.1f years", years);
        } else if (days > 60) {
            double months = days / 30.44;
            return String.format("%.0f months", months);
        } else {
            return String.format("%.0f days", days);
        }
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
