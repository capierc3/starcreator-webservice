package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.probabilityreport.ProbabilityReportGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Local debug test for the API-based ProbabilityReportGenerator.
 * Calls the same code path as POST /api/v1/system-probability-test
 * but runs directly — no HTTP, no auth needed.
 *
 * Uncomment @Test to run. Output goes to target/probability_reports/
 */
@SpringBootTest
@ActiveProfiles("test")
public class ProbabilityReportApiTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final int[] RANGES = new int[] {10, 100, 1_000, 10_000, 100_000};
    //                                              0   1     2       3       4
    private static final int SYSTEM_AMOUNT = RANGES[1];

    //@Test
    public void runProbabilityReportGenerator() {
        ProbabilityReportGenerator generator = new ProbabilityReportGenerator(systemCreator, SYSTEM_AMOUNT);
        generator.generate();
    }
}
