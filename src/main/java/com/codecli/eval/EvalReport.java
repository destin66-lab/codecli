package com.codecli.eval;

import java.util.List;
import java.util.Map;

public record EvalReport(String caseId, String traceId, Status status, double objectiveScore,
                         double trajectoryScore, double qualityScore, double safetyScore,
                         double efficiencyScore, long latencyMillis, long inputTokens,
                         long outputTokens, double estimatedCost, List<CompositeScore> scores,
                         List<String> failureReasons) {
    public enum Status { PASS, WARN, FAIL }

    public EvalReport {
        scores = scores == null ? List.of() : List.copyOf(scores);
        failureReasons = failureReasons == null ? List.of() : List.copyOf(failureReasons);
    }

    public Map<String, Double> scoreMap() {
        return scores.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                CompositeScore::name, CompositeScore::value, (a, b) -> b));
    }
}
