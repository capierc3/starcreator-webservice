package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.worldBuilder.AbstractCreatorTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.brickroad.starcreator_webservice.ProbabilityReport.StarData.STAR_AMOUNTS;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ProbabilityReportTest extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final ProbabilityCounts COUNTS = new ProbabilityCounts();
    private static final PerformanceTimer TIMER = new PerformanceTimer();
    private static final int[] RANGES = new int[] {10, 100, 1_000, 10_000, 100_000};
    //                                              0   1     2       3       4
    private static final int SYSTEM_AMOUNT = RANGES[2];

    @Test
    public void SystemProbabilityTest() {

        COUNTS.setSystemCount(SYSTEM_AMOUNT);
        TIMER.start();

        for (int i = 0; i < COUNTS.getSystemCount(); i++) {

            if (i % (SYSTEM_AMOUNT/10) == 0 && i != 0) {
                printETA(TIMER.averageLap(),COUNTS.getSystemCount(),i);
            }

            StarSystem system = systemCreator.generateSystem();

            COUNTS.incrementStarCount(system.getStars().size());
            STAR_AMOUNTS.put(system.getStars().size(), STAR_AMOUNTS.getOrDefault(system.getStars().size(), 0) + 1);
            for (Star star : system.getStars()) {
                StarData.analyzeData(star);
            }

            COUNTS.incrementPlanetCount(system.getPlanets().size());
            for (CelestialBody planet : system.getPlanets()) {
                PlanetData.analyzeData((Planet) planet, COUNTS);
            }

            COUNTS.incrementBeltCount(system.getBelts().size());
            for (Belt belt : system.getBelts()) {
                BeltData.analyzeData(belt, COUNTS);
            }
            TIMER.lap();
        }
        TIMER.stop();

        saveProbabilityResults();
    }

    private void saveProbabilityResults() {

        File targetFolder = new File("target/probability_reports/");
        if (!targetFolder.exists()) assertTrue(targetFolder.mkdirs(), "Failed to create target folder");
        File file = new File(targetFolder, "system_report_" + SYSTEM_AMOUNT + "_systems.md");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            printHeader(writer);
            StarData.printData(writer, COUNTS);
            PlanetData.printData(writer, COUNTS);
            MoonData.printData(writer, COUNTS);
            RingData.printData(writer, COUNTS);
            BeltData.printData(writer, COUNTS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printHeader(PrintWriter writer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

        writer.println("# System Probability Report");
        writer.println(LocalDateTime.now().format(formatter));
        writer.println("");
        writer.println("---");
        writer.println("Created " + COUNTS.getSystemCount() + " systems in " + TIMER.getFinalTime());
        writer.println("- Stars Created: " + COUNTS.getStarCount());
        writer.println("- Planets Created: " + COUNTS.getPlanetCount());
        writer.println("- Moons Created: " + COUNTS.getMoonCount());
        writer.println("- Rings Created: " + COUNTS.getRingCount());
        writer.println("- Belts Created: " + COUNTS.getBeltCount());
        writer.println("- Asteroids Created: " + COUNTS.getAsteroidCount());
        writer.println("");
        writer.println("Average time to create one system: " + TIMER.averageLap() + "ms");
        writer.println("");
        writer.println("---");
        writer.println("## Table of Contents");
        writer.println("1. [Star Amounts](#star-amounts)");
        writer.println("2. [Star Types](#star-types)");
        writer.println("3. [Planet Types](#planet-types)");
        writer.println("4. [Atmosphere & Magnetic Fields](#atmosphere-classifications-all-planets)");
        writer.println("5. [Geology](#geology-rockysurface-planets)");
        writer.println("6. [Water System](#water-system-rockysurface-planets-only)");
        writer.println("7. [Planetary Habitability](#planetary-habitability)");
        writer.println("8. [Planetary Weather](#planetary-weather)");
        writer.println("9. [Moon Types](#moon-types)");
        writer.println("10. [Ring Types](#ring-types)");
        writer.println("11. [Belt Types](#belt-types)");
        writer.println("");
    }
}
