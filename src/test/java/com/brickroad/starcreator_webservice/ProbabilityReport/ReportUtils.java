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
}