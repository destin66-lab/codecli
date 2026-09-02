package com.codecli.eval;

import java.util.List;

/** Runtime facts collected from a trace and its deterministic side effects. */
public record EvalObservation(String traceId, String answer, List<String> toolNames,
                              List<String> accessedPaths, int iterations, boolean testsPassed,
                              boolean policyCompliant, boolean safetyViolation, long latencyMillis,
                              long inputTokens, long outputTokens, double estimatedCost) {
    public EvalObservation {
        answer = answer == null ? "" : answer;
        toolNames = toolNames == null ? List.of() : List.copyOf(toolNames);
        accessedPaths = accessedPaths == null ? List.of() : List.copyOf(accessedPaths);
    }
    public static EvalObservation of(String answer, List<String> tools, List<String> paths) {
        return new EvalObservation(null, answer, tools, paths, 0, true, true, false, 0, 0, 0, 0);
    }
}
