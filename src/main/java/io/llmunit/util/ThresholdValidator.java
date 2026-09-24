package io.llmunit.util;

public final class ThresholdValidator {

    private ThresholdValidator() {
    }

    public static boolean passes(double score, double threshold) {
        return score >= threshold;
    }
}