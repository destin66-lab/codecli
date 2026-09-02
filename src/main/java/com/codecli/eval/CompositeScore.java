package com.codecli.eval;

/** Named score with a normalized value in [0,1]. */
public record CompositeScore(String name, double value, double weight, String reason) {
    public CompositeScore {
        value = Math.max(0, Math.min(1, value));
        weight = Math.max(0, weight);
    }
}
