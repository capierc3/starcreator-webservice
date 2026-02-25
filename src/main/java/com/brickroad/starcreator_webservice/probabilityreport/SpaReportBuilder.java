package com.brickroad.starcreator_webservice.probabilityreport;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Generates a self-contained SPA (Single Page Application) HTML report.
 * <p>
 * The SPA embeds all report data as a JavaScript constant and renders
 * all content client-side using vanilla JS. Navigation between pages
 * (Dashboard, Stars, Planets, etc.) uses hash-based routing.
 * <p>
 * The System Viewer page can optionally connect to the live REST API
 * to generate and visualize new star systems.
 */
public class SpaReportBuilder {

    private final Map<String, Object> reportData;
    private final ProbabilityCounts counts;
    private final PerformanceTimer timer;

    public SpaReportBuilder(Map<String, Object> reportData,
                            ProbabilityCounts counts,
                            PerformanceTimer timer) {
        this.reportData = reportData;
        this.counts = counts;
        this.timer = timer;
    }

    public void saveReport(File targetFolder) {
        File file = new File(targetFolder,
                "system_spa_report_" + counts.getSystemCount() + "_systems.html");

        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(reportData);

            w.println("<!DOCTYPE html>");
            w.println("<html lang=\"en\">");
            w.println("<head>");
            w.println("<meta charset=\"UTF-8\">");
            w.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            w.println("<title>StarCreator — Probability Report</title>");
            w.println("<link href=\"https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@300;400;500;600;700&family=Outfit:wght@200;300;400;500;600;700&display=swap\" rel=\"stylesheet\">");
            w.println("<style>");
            w.println(SpaReportCssTemplate.CSS);
            w.println("</style>");
            w.println("</head>");
            w.println("<body>");

            // Embed report data as JS constant
            w.println("<script>");
            w.print("const REPORT_DATA = ");
            w.print(jsonData);
            w.println(";");
            w.println("</script>");

            // SPA engine (router, components, page renderers)
            w.println("<script>");
            w.println(SpaReportJsTemplate.js());
            w.println("</script>");

            // Orbital simulation module (for System Viewer page)
            w.println("<script>");
            w.println(OrbitalSimJsTemplate.JS);
            w.println("</script>");

            w.println("</body>");
            w.println("</html>");

        } catch (IOException e) {
            System.err.println("Failed to save SPA report: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("SPA report saved to: " + file.getAbsolutePath());
    }
}
