package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.probabilityreport.ProbabilityReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProbabilityReportService {

    @Autowired
    private SystemCreator systemCreator;

    @Async("reportExecutor")
    public void generateReportAsync(int systemCount) {
        System.out.println("Starting probability report generation for " + systemCount + " systems...");
        try {
            ProbabilityReportGenerator generator = new ProbabilityReportGenerator(systemCreator, systemCount);
            generator.generate();
            System.out.println("Probability report generation completed for " + systemCount + " systems.");
        } catch (Exception e) {
            System.err.println("Probability report generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
