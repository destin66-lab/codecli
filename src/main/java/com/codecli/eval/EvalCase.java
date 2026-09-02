package com.codecli.eval;

import java.util.List;

/** A reproducible evaluation case. Optional expectations may be empty. */
public record EvalCase(String id, String input, List<String> expectedTools, List<String> expectedPaths,
                       String expectedText, int maxIterations) {
    public EvalCase {
        id = id == null || id.isBlank() ? "case" : id;
        input = input == null ? "" : input;
        expectedTools = expectedTools == null ? List.of() : List.copyOf(expectedTools);
        expectedPaths = expectedPaths == null ? List.of() : List.copyOf(expectedPaths);
        expectedText = expectedText == null ? "" : expectedText;
        maxIterations = maxIterations <= 0 ? Integer.MAX_VALUE : maxIterations;
    }
}
