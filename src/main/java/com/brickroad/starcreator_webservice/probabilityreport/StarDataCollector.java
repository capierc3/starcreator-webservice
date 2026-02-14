package com.brickroad.starcreator_webservice.probabilityreport;

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
        int planetCount = star.getSystem() != null ? star.getSystem().getPlanets().size() : 0;
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
}
