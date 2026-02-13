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
public class HtmlProbabilityReportTest extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final ProbabilityCounts COUNTS = new ProbabilityCounts();
    private static final PerformanceTimer TIMER = new PerformanceTimer();
    private static final int[] RANGES = new int[] {10, 100, 1_000, 10_000, 100_000};
    //                                              0   1     2       3       4
    private static final int SYSTEM_AMOUNT = RANGES[2];

    @Test
    public void SystemProbabilityHtmlReport() {

        COUNTS.setSystemCount(SYSTEM_AMOUNT);
        TIMER.start();

        for (int i = 0; i < COUNTS.getSystemCount(); i++) {

            if (i % (SYSTEM_AMOUNT / 10) == 0 && i != 0) {
                printETA(TIMER.averageLap(), COUNTS.getSystemCount(), i);
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

        saveHtmlReport();
    }

    private void saveHtmlReport() {

        File targetFolder = new File("target/probability_reports/");
        if (!targetFolder.exists()) assertTrue(targetFolder.mkdirs(), "Failed to create target folder");
        File file = new File(targetFolder, "system_report_" + SYSTEM_AMOUNT + "_systems.html");

        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
            HtmlReportUtils.printPageHeader(w);
            printHtmlHeader(w);
            StarData.printHtml(w, COUNTS);
            PlanetData.printHtml(w, COUNTS);
            MoonData.printHtml(w, COUNTS);
            RingData.printHtml(w, COUNTS);
            BeltData.printHtml(w, COUNTS);
            HtmlReportUtils.printPageFooter(w);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("HTML report saved to: " + file.getAbsolutePath());
    }

    private void printHtmlHeader(PrintWriter w) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

        // Header Card
        w.println("<div class=\"report-header\">");
        w.println("<h1>System Probability Report</h1>");
        w.println("<p class=\"subtitle\">" + LocalDateTime.now().format(formatter) + "</p>");
        w.println("<p class=\"subtitle\">Created " + COUNTS.getSystemCount() + " systems in " + TIMER.getFinalTime() + "</p>");
        w.println("</div>");

        // Stats Grid
        w.println("<div class=\"stats-grid\">");
        printStatCard(w, String.valueOf(COUNTS.getSystemCount()), "Systems");
        printStatCard(w, String.valueOf(COUNTS.getStarCount()), "Stars");
        printStatCard(w, String.valueOf(COUNTS.getPlanetCount()), "Planets");
        printStatCard(w, String.valueOf(COUNTS.getMoonCount()), "Moons");
        printStatCard(w, String.valueOf(COUNTS.getRingCount()), "Rings");
        printStatCard(w, String.valueOf(COUNTS.getBeltCount()), "Belts");
        printStatCard(w, String.valueOf(COUNTS.getAsteroidCount()), "Asteroids");
        printStatCard(w, TIMER.averageLap() + "ms", "Avg / System");
        w.println("</div>");

        // Table of Contents
        w.println("<nav class=\"toc\">");
        w.println("<h2>Table of Contents</h2>");
        w.println("<ol>");
        w.println("<li><a href=\"#star-amounts\">Star Amounts</a></li>");
        w.println("<li><a href=\"#star-types\">Star Types</a></li>");
        w.println("<li><a href=\"#planet-types\">Planet Types</a></li>");
        w.println("<li><a href=\"#atmosphere--magnetic-fields\">Atmosphere &amp; Magnetic Fields</a></li>");
        w.println("<li><a href=\"#geology-rockysurface-planets\">Geology</a></li>");
        w.println("<li><a href=\"#water-system-rockysurface-planets-only\">Water System</a></li>");
        w.println("<li><a href=\"#planetary-habitability\">Planetary Habitability</a></li>");
        w.println("<li><a href=\"#planetary-weather\">Planetary Weather</a></li>");
        w.println("<li><a href=\"#moon-types\">Moon Types</a></li>");
        w.println("<li><a href=\"#ring-types\">Ring Types</a></li>");
        w.println("<li><a href=\"#belt-types\">Belt Types</a></li>");
        w.println("</ol>");
        w.println("</nav>");
    }

    private void printStatCard(PrintWriter w, String value, String label) {
        w.println("<div class=\"stat-card\">");
        w.println("<span class=\"stat-value\">" + value + "</span>");
        w.println("<span class=\"stat-label\">" + label + "</span>");
        w.println("</div>");
    }
}
