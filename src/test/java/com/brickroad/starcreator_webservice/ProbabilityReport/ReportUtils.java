package com.brickroad.starcreator_webservice.ProbabilityReport;

import java.io.PrintWriter;
import java.util.Map;

public class ReportUtils {

    static String pct(int count, int total) {
        return String.format("%.1f", count * 100.0 / Math.max(1, total));
    }

    static String pct(double count, int total) {
        return String.format("%.1f", count * 100.0 / Math.max(1, total));
    }

    static void printSortedTable(PrintWriter writer, Map<String, Integer> data, int total, String col1Name) {
        writer.println("| " + col1Name + " | Count | % |");
        writer.println("| --- | --- | --- |");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue()
                        + " | " + pct(e.getValue(), total) + "% |"));
        writer.println("");
    }

    static void printSortedTableByKey(PrintWriter writer, Map<String, Integer> data, int total, String col1Name) {
        writer.println("| " + col1Name + " | Count | % |");
        writer.println("| --- | --- | --- |");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue()
                        + " | " + pct(e.getValue(), total) + "% |"));
        writer.println("");
    }

    static void printLinkedTable(PrintWriter writer, Map<String, Integer> data, int total, String col1Name) {
        writer.println("| " + col1Name + " | Count | % |");
        writer.println("| --- | --- | --- |");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    String anchor = toAnchor(e.getKey());
                    writer.println("| [" + e.getKey() + "](#" + anchor + ") | "
                            + e.getValue() + " | " + pct(e.getValue(), total) + "% |");
                });
        writer.println("");
    }

    static String toAnchor(String text) {
        return text.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9\\-]", "");
    }

    static void printAnchor(PrintWriter writer, String text) {
        writer.println("<a id=\"" + toAnchor(text) + "\"></a>");
    }

    static void printSection(PrintWriter writer, String title) {
        writer.println("---");
        writer.println("## " + title);
        writer.println("");
    }

    static void printSubSection(PrintWriter writer, String title) {
        writer.println("### " + title);
        writer.println("");
    }

    static void printSubSubSection(PrintWriter writer, String title) {
        writer.println("#### " + title);
        writer.println("");
    }

    static void beginCollapsible(PrintWriter writer, String title, int headingLevel) {
        String hTag = "h" + headingLevel;
        writer.println("<details>");
        writer.println("<summary><" + hTag + ">" + title + "</" + hTag + "></summary>");
        writer.println("");
    }

    static void beginCollapsibleOpen(PrintWriter writer, String title, int headingLevel) {
        String hTag = "h" + headingLevel;
        writer.println("<details open>");
        writer.println("<summary><" + hTag + ">" + title + "</" + hTag + "></summary>");
        writer.println("");
    }

    static void endCollapsible(PrintWriter writer) {
        writer.println("</details>");
        writer.println("");
    }
}