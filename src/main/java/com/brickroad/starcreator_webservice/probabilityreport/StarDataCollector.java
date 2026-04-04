package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.OrbitalElements;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class StarDataCollector {

    private final Map<String, Integer> starTypes = new HashMap<>();
    private final Map<String, Map<String, Map<String, Integer>>> starTypesData = new HashMap<>();
    private final Map<Integer, Integer> starAmounts = new HashMap<>();
    private final Map<String, Integer> starRoles = new HashMap<>();
    private final Map<String, Integer> binaryConfigurations = new HashMap<>();

    // Planet formations per star type: starType -> planetType -> [count, minAU, maxAU]
    private final Map<String, Map<String, double[]>> planetFormationsByStarType = new HashMap<>();

    // Companion star orbital element distributions (non-primary stars only)
    private final Map<String, Integer> companionEccentricityBins = new HashMap<>();
    private final Map<String, Integer> companionInclinationBins = new HashMap<>();
    private final Map<String, Integer> companionSeparationBins = new HashMap<>();
    private final Map<String, Integer> companionOrbitalPeriodBins = new HashMap<>();

    public void analyzeData(Star star) {
        starTypes.merge(star.getType(), 1, Integer::sum);

        String role = star.getStarRole() != null ? star.getStarRole().name() : "UNKNOWN";
        starRoles.merge(role, 1, Integer::sum);

        Map<String, Map<String, Integer>> typeData = starTypesData.computeIfAbsent(star.getType(), k -> new HashMap<>());

        Map<String, Integer> activityData = typeData.computeIfAbsent("activity", k -> new HashMap<>());
        activityData.merge(star.getActivityLevel(), 1, Integer::sum);

        Map<String, Integer> flareData = typeData.computeIfAbsent("flare class", k -> new HashMap<>());
        flareData.merge(star.getFlareClass(), 1, Integer::sum);

        Map<String, Integer> spotData = typeData.computeIfAbsent("spot %", k -> new HashMap<>());
        String spotBin = binSpotCoverage(star.getStarspotCoveragePercent() != null
                ? star.getStarspotCoveragePercent() : 0.0);
        spotData.merge(spotBin, 1, Integer::sum);

        Map<String, Integer> xrayData = typeData.computeIfAbsent("xray Luminosity", k -> new HashMap<>());
        xrayData.merge(star.getXrayLuminosityClass(), 1, Integer::sum);

        Map<String, Integer> evoData = typeData.computeIfAbsent("evolutionary stage", k -> new HashMap<>());
        evoData.merge(star.getEvolutionaryStage(), 1, Integer::sum);

        Map<String, Integer> planetCountData = typeData.computeIfAbsent("planets per system", k -> new HashMap<>());
        int planetCount = 0;
        if (star.getSystem() != null) {
            Map<String, double[]> formations = planetFormationsByStarType.computeIfAbsent(star.getType(), k -> new HashMap<>());
            for (Planet planet : star.getSystem().getPlanets()) {
                if (planet.getParentStar() == star) {
                    planetCount++;
                    String pType = planet.getPlanetType() != null ? planet.getPlanetType() : "UNKNOWN";
                    double au = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : -1;
                    double[] stats = formations.get(pType);
                    if (stats == null) {
                        // [count, minAU, maxAU]
                        stats = new double[]{1, au, au};
                        formations.put(pType, stats);
                    } else {
                        stats[0]++;
                        if (au >= 0) {
                            stats[1] = stats[1] < 0 ? au : Math.min(stats[1], au);
                            stats[2] = stats[2] < 0 ? au : Math.max(stats[2], au);
                        }
                    }
                }
            }
        }
        String planetBin = binPlanetCount(planetCount);
        planetCountData.merge(planetBin, 1, Integer::sum);

        // Habitable zone bins — exclude P-type binaries (HZ is based on dual stars, not the individual type)
        boolean isPType = star.getSystem() != null
                && star.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE;

        if (!isPType) {
            if (star.getHabitableZoneInnerAU() != null) {
                Map<String, Integer> hzInnerData = typeData.computeIfAbsent("hz inner AU", k -> new HashMap<>());
                hzInnerData.merge(binHzAU(star.getHabitableZoneInnerAU()), 1, Integer::sum);
            }
            if (star.getHabitableZoneOuterAU() != null) {
                Map<String, Integer> hzOuterData = typeData.computeIfAbsent("hz outer AU", k -> new HashMap<>());
                hzOuterData.merge(binHzAU(star.getHabitableZoneOuterAU()), 1, Integer::sum);
            }
        }

        // Companion star orbital elements (skip primaries at origin)
        if (star.getStarRole() != null && star.getStarRole() != Star.StarRole.PRIMARY) {
            OrbitalElements orbit = star.getOrbit();
            if (orbit != null && orbit.getSemiMajorAxis() != null && orbit.getSemiMajorAxis() > 0) {
                if (orbit.getEccentricity() != null) {
                    companionEccentricityBins.merge(binEccentricity(orbit.getEccentricity()), 1, Integer::sum);
                }
                if (orbit.getInclinationDegrees() != null) {
                    companionInclinationBins.merge(binInclination(orbit.getInclinationDegrees()), 1, Integer::sum);
                }
                companionSeparationBins.merge(binSeparation(orbit.getSemiMajorAxis()), 1, Integer::sum);
                if (orbit.getOrbitalPeriodDays() != null) {
                    companionOrbitalPeriodBins.merge(binOrbitalPeriod(orbit.getOrbitalPeriodDays()), 1, Integer::sum);
                }
            }
        }
    }

    public boolean isUniformType(Map<String, Map<String, Integer>> typeData) {
        for (Map<String, Integer> subcategory : typeData.values()) {
            if (subcategory.size() > 1) return false;
        }
        return true;
    }

    private String binSpotCoverage(double pct) {
        if (pct < 0.1) return "0% (none)";
        if (pct < 1.0) return "<1% (trace)";
        if (pct <= 2) return "1-2% (low)";
        if (pct <= 5) return "3-5% (moderate)";
        if (pct <= 10) return "6-10% (high)";
        if (pct <= 25) return "11-25% (very high)";
        return "26%+ (extreme)";
    }

    private String binPlanetCount(int count) {
        if (count == 0) return "0";
        if (count <= 2) return "1-2";
        if (count <= 4) return "3-4";
        if (count <= 6) return "5-6";
        if (count <= 8) return "7-8";
        return "9+";
    }

    private String binHzAU(double au) {
        if (au < 0.01) return "<0.01 AU";
        if (au < 0.05) return "0.01-0.05 AU";
        if (au < 0.1) return "0.05-0.1 AU";
        if (au < 0.2) return "0.1-0.2 AU";
        if (au < 0.5) return "0.2-0.5 AU";
        if (au < 1.0) return "0.5-1.0 AU";
        if (au < 2.0) return "1.0-2.0 AU";
        if (au < 5.0) return "2.0-5.0 AU";
        if (au < 10.0) return "5.0-10.0 AU";
        if (au < 50.0) return "10-50 AU";
        return "50+ AU";
    }

    private String binEccentricity(double ecc) {
        if (ecc < 0.05) return "0.00-0.05 (circular)";
        if (ecc < 0.15) return "0.05-0.15 (near-circular)";
        if (ecc < 0.3) return "0.15-0.30 (moderate)";
        if (ecc < 0.5) return "0.30-0.50 (eccentric)";
        if (ecc < 0.7) return "0.50-0.70 (highly eccentric)";
        return "0.70+ (extreme)";
    }

    private String binInclination(double deg) {
        if (deg < 5) return "0-5° (aligned)";
        if (deg < 15) return "5-15° (low tilt)";
        if (deg < 30) return "15-30° (moderate)";
        if (deg < 60) return "30-60° (inclined)";
        if (deg < 90) return "60-90° (polar)";
        if (deg < 120) return "90-120° (retrograde)";
        return "120°+ (highly retrograde)";
    }

    private String binSeparation(double au) {
        if (au < 0.5) return "<0.5 AU (contact)";
        if (au < 2) return "0.5-2 AU (close)";
        if (au < 10) return "2-10 AU (moderate)";
        if (au < 50) return "10-50 AU (wide)";
        if (au < 200) return "50-200 AU (very wide)";
        return "200+ AU (distant)";
    }

    private String binOrbitalPeriod(double days) {
        if (days < 30) return "<1 month";
        if (days < 365) return "1 month - 1 year";
        if (days < 3652) return "1-10 years";
        if (days < 36525) return "10-100 years";
        if (days < 365250) return "100-1000 years";
        return "1000+ years";
    }
}
