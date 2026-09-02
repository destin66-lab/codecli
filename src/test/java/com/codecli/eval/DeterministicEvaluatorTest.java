package com.codecli.eval;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

class DeterministicEvaluatorTest {
    @Test void loadsExistingGoldenSetShape() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/code-search/golden-set.json")) {
            assertNotNull(in);
            assertEquals(5, EvalRunner.loadCases(in).size());
        }
    }
    @Test void passesGoldenLikeCase() {
        EvalCase expected = new EvalCase("x", "find", List.of("grep_code", "read_file"),
                List.of("src/Main.java"), "answer", 3);
        EvalObservation actual = new EvalObservation("trace-1", "The answer is here", 
                List.of("grep_code", "read_file"), List.of("src/Main.java"), 2,
                true, true, false, 120, 10, 5, 0.01);
        EvalReport report = new DeterministicEvaluator().evaluate(expected, actual);
        assertEquals(EvalReport.Status.PASS, report.status());
        assertEquals(1.0, report.scoreMap().get("task_success"));
    }

    @Test void safetyViolationIsHardFail() {
        EvalReport report = new DeterministicEvaluator().evaluate(
                new EvalCase("x", "", List.of(), List.of(), "", 1),
                new EvalObservation(null, "done", List.of(), List.of(), 1, true, false, true, 0, 0, 0, 0));
        assertEquals(EvalReport.Status.FAIL, report.status());
        assertTrue(report.failureReasons().contains("policy_compliant=false"));
    }
}
