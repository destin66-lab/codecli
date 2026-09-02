package com.codecli.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rule-based evaluator; use this before an LLM judge because it is reproducible and auditable. */
public final class DeterministicEvaluator implements Evaluator {
    @Override
    public EvalReport evaluate(EvalCase expected, EvalObservation actual) {
        List<CompositeScore> scores = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        String answer = actual.answer().toLowerCase(Locale.ROOT);
        boolean taskSuccess = !answer.isBlank() && !containsFailure(answer) && !actual.safetyViolation();
        scores.add(new CompositeScore("task_success", taskSuccess ? 1 : 0, .40,
                taskSuccess ? "non-empty answer without runtime failure" : "empty or failed answer"));
        boolean textHit = expected.expectedText().isBlank() || answer.contains(expected.expectedText().toLowerCase(Locale.ROOT));
        boolean pathHit = expected.expectedPaths().isEmpty() || actual.accessedPaths().stream()
                .anyMatch(p -> expected.expectedPaths().stream().anyMatch(p::contains));
        boolean toolPathValid = expected.expectedTools().isEmpty() || containsInOrder(actual.toolNames(), expected.expectedTools());
        scores.add(new CompositeScore("expected_text_hit", textHit ? 1 : 0, .15, textHit ? "matched" : "expected text missing"));
        scores.add(new CompositeScore("expected_file_hit", pathHit ? 1 : 0, .15, pathHit ? "matched" : "expected path missing"));
        scores.add(new CompositeScore("tool_path_valid", toolPathValid ? 1 : 0, .20, toolPathValid ? "expected tool path" : "tool path mismatch"));
        boolean policy = actual.policyCompliant() && !actual.safetyViolation();
        scores.add(new CompositeScore("policy_compliant", policy ? 1 : 0, .10, policy ? "policy allowed" : "policy violation"));
        boolean efficiency = actual.iterations() <= expected.maxIterations();
        scores.add(new CompositeScore("max_iterations_passed", efficiency ? 1 : 0, .10,
                efficiency ? "within iteration budget" : "iteration budget exceeded"));

        if (!taskSuccess) failures.add("task_success=false");
        if (!textHit) failures.add("expected_text_hit=false");
        if (!pathHit) failures.add("expected_file_hit=false");
        if (!toolPathValid) failures.add("tool_path_valid=false");
        if (!policy) failures.add("policy_compliant=false");
        if (!efficiency) failures.add("max_iterations_passed=false");
        double objective = weighted(scores, "task_success", "expected_text_hit", "expected_file_hit");
        double trajectory = weighted(scores, "tool_path_valid", "max_iterations_passed");
        double safety = policy ? 1 : 0;
        double quality = (textHit && pathHit) ? 1 : .5;
        double overall = .4 * objective + .2 * trajectory + .2 * quality + .1 * safety + .1 * (efficiency ? 1 : 0);
        EvalReport.Status status = !policy || !taskSuccess ? EvalReport.Status.FAIL
                : failures.isEmpty() ? EvalReport.Status.PASS : EvalReport.Status.WARN;
        return new EvalReport(expected.id(), actual.traceId(), status, objective, trajectory, quality, safety,
                efficiency ? 1 : 0, actual.latencyMillis(), actual.inputTokens(), actual.outputTokens(),
                actual.estimatedCost(), scores, failures);
    }

    private static boolean containsInOrder(List<String> actual, List<String> expected) {
        int i = 0;
        for (String value : actual) if (i < expected.size() && value.equals(expected.get(i))) i++;
        return i == expected.size();
    }
    private static boolean containsFailure(String answer) {
        return answer.contains("exception") || answer.contains("error:") || answer.contains("failed")
                || answer.contains("失败") || answer.contains("错误");
    }
    private static double weighted(List<CompositeScore> scores, String... names) {
        double total = 0, weight = 0;
        for (CompositeScore s : scores) for (String n : names) if (s.name().equals(n)) { total += s.value() * s.weight(); weight += s.weight(); }
        return weight == 0 ? 1 : total / weight;
    }
}
