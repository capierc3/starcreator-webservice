package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.*;

import java.io.File;

public class ProbabilityReportGenerator {

    private final SystemCreator systemCreator;
    private final int systemCount;

    private final ProbabilityCounts counts = new ProbabilityCounts();
    private final PerformanceTimer timer = new PerformanceTimer();
    private final StarDataCollector starData = new StarDataCollector();
    private final MoonDataCollector moonData = new MoonDataCollector();
    private final RingDataCollector ringData = new RingDataCollector();
    private final BeltDataCollector beltData = new BeltDataCollector();
    private final PlanetDataCollector planetData;

    public ProbabilityReportGenerator(SystemCreator systemCreator, int systemCount) {
        this.systemCreator = systemCreator;
        this.systemCount = systemCount;
        this.planetData = new PlanetDataCollector(moonData, ringData);
    }

    public void generate() {
        counts.setSystemCount(systemCount);
        timer.start();

        for (int i = 0; i < systemCount; i++) {
            if (systemCount >= 10 && i % (systemCount / 10) == 0 && i != 0) {
                printETA(timer.averageLap(), systemCount, i);
            }

            StarSystem system = systemCreator.generateSystem();

            counts.incrementStarCount(system.getStars().size());
            starData.getStarAmounts().merge(system.getStars().size(), 1, Integer::sum);
            for (Star star : system.getStars()) {
                starData.analyzeData(star);
            }

            counts.incrementPlanetCount(system.getPlanets().size());
            for (CelestialBody planet : system.getPlanets()) {
                planetData.analyzeData((Planet) planet, counts);
            }

            counts.incrementBeltCount(system.getBelts().size());
            for (Belt belt : system.getBelts()) {
                beltData.analyzeData(belt, counts);
            }
            timer.lap();
        }
        timer.stop();

        // Create output folder
        File targetFolder = new File("target/probability_reports/");
        if (!targetFolder.exists()) {
            if (!targetFolder.mkdirs()) {
                System.err.println("Failed to create target folder: " + targetFolder.getAbsolutePath());
                return;
            }
        }

        // Generate HTML report
        HtmlReportBuilder htmlBuilder = new HtmlReportBuilder(counts, timer, starData, planetData, moonData, ringData, beltData);
        htmlBuilder.saveReport(targetFolder);

        // Generate JSON report
        try {
            JsonReportBuilder jsonBuilder = new JsonReportBuilder(counts, timer, starData, planetData, moonData, ringData, beltData);
            jsonBuilder.saveReport(targetFolder);
        } catch (Exception e) {
            System.err.println("Failed to save JSON report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printETA(double averageTimePerSystem, int systemsAmount, int i) {
        int remainingSystems = systemsAmount - i;
        double estimatedRemainingMs = averageTimePerSystem * remainingSystems;

        int minutes = (int) (estimatedRemainingMs / 60000);
        int seconds = (int) ((estimatedRemainingMs % 60000) / 1000);

        System.out.printf("%d created, Estimated time remaining: %d min %d sec%n",
                i, minutes, seconds);
    }
}
