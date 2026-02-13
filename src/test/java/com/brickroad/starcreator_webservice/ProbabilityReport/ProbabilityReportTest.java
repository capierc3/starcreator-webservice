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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    private static final String HEADER_IMAGE_SOURCE = ".scratch/headerImg.png";
    private static final String HEADER_IMAGE_FILENAME = "headerImg.png";

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

        // Generate both reports from the same data
        File targetFolder = new File("target/probability_reports/");
        if (!targetFolder.exists()) assertTrue(targetFolder.mkdirs(), "Failed to create target folder");

        saveMarkdownReport(targetFolder);
        saveHtmlReport(targetFolder);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Markdown Report
    // ═══════════════════════════════════════════════════════════════

    private void saveMarkdownReport(File targetFolder) {
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

        System.out.println("Markdown report saved to: " + file.getAbsolutePath());
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
        writer.println("9. [Surface Planet Weather](#surface-planet-weather)");
        writer.println("10. [Gas / Ice Giant Weather](#gas--ice-giant-weather)");
        writer.println("11. [Moon Types](#moon-types)");
        writer.println("12. [Ring Types](#ring-types)");
        writer.println("13. [Belt Types](#belt-types)");
        writer.println("");
    }

    // ═══════════════════════════════════════════════════════════════
    //  HTML Report
    // ═══════════════════════════════════════════════════════════════

    private void saveHtmlReport(File targetFolder) {
        File file = new File(targetFolder, "system_report_" + SYSTEM_AMOUNT + "_systems.html");

        // Copy header image next to the HTML report
        String headerImagePath = copyHeaderImage(targetFolder);

        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {

            // Page shell + hero banner + sidebar TOC opens
            HtmlReportUtils.printPageHeader(w, headerImagePath);

            // Write sidebar TOC content
            printSidebarToc(w);

            // Close sidebar, open <main>
            HtmlReportUtils.beginMainContent(w);

            // Report header card + stats grid (inside main content)
            printHtmlHeader(w);

            // Data sections
            StarData.printHtml(w, COUNTS);
            PlanetData.printHtml(w, COUNTS);
            MoonData.printHtml(w, COUNTS);
            RingData.printHtml(w, COUNTS);
            BeltData.printHtml(w, COUNTS);

            // Close main + page
            HtmlReportUtils.printPageFooter(w);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("HTML report saved to: " + file.getAbsolutePath());
    }

    /**
     * Copy the header image to the report output folder so the HTML file can
     * reference it with a relative path. Returns the relative path string,
     * or null if the source image doesn't exist.
     */
    private String copyHeaderImage(File targetFolder) {
        Path source = Path.of(HEADER_IMAGE_SOURCE);
        if (!Files.exists(source)) {
            System.out.println("Header image not found at " + source.toAbsolutePath() + " — skipping.");
            return null;
        }
        try {
            Path dest = targetFolder.toPath().resolve(HEADER_IMAGE_FILENAME);
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return HEADER_IMAGE_FILENAME; // relative to the HTML file
        } catch (IOException e) {
            System.err.println("Failed to copy header image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Wikipedia-style sidebar TOC. Each href must match the id generated by
     * {@link HtmlReportUtils#beginCollapsible} for the corresponding section.
     */
    private void printSidebarToc(PrintWriter w) {
        w.println("<div class=\"toc-title\">Contents</div>");
        w.println("<ol>");
        tocLink(w, "Star Amounts");
        tocLink(w, "Planet Types");
        tocLink(w, "Per-Planet-Type Breakdown");
        tocLink(w, "Atmosphere & Magnetic Fields");
        tocLink(w, "Geology (Rocky/Surface Planets)");
        tocLink(w, "Water System (Rocky/Surface Planets Only)");
        tocLink(w, "Planetary Habitability");
        tocLink(w, "Planetary Weather");
        tocLink(w, "Surface Planet Weather");
        tocLink(w, "Gas / Ice Giant Weather");
        tocLink(w, "Moon Types");
        tocLink(w, "Ring Types");
        tocLink(w, "Belt Types");
        w.println("</ol>");
    }

    private void tocLink(PrintWriter w, String sectionTitle) {
        String anchor = HtmlReportUtils.toAnchor(sectionTitle);
        w.println("<li><a href=\"#" + anchor + "\">" + HtmlReportUtils.esc(sectionTitle) + "</a></li>");
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
    }

    private void printStatCard(PrintWriter w, String value, String label) {
        w.println("<div class=\"stat-card\">");
        w.println("<span class=\"stat-value\">" + value + "</span>");
        w.println("<span class=\"stat-label\">" + label + "</span>");
        w.println("</div>");
    }
}
