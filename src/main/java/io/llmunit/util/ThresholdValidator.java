package io.llmunit.util;

/**
 * Utility for comparing evaluation scores against their pass/fail thresholds.
 */
public final class ThresholdValidator {

    private ThresholdValidator() {
    }

    public static boolean passes(double score, double threshold) {
        return score >= threshold;
    }
}