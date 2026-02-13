package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.Ring;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class RingData {

    private static final Map<String, Integer> RING_TYPES = new HashMap<>();

    static void analyzeData(Ring ring) {
        RING_TYPES.put(ring.getRingType(), RING_TYPES.getOrDefault(ring.getRingType(), 0) + 1);
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Ring Types", 2);
        ReportUtils.printSortedTable(writer, RING_TYPES, counts.getRingCount(), "Ring Type");
        ReportUtils.endCollapsible(writer);
    }
}
