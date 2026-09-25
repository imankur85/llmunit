package io.llmunit.core;

/**
 * Immutable outcome of a single evaluation: the score versus its threshold, human-readable
 * feedback, and whether the metric passed.
 */
public final class EvalResult {

    private final String metricName;
    private final double score;
    private final double threshold;
    private final String feedback;
    private final boolean passed;

    public EvalResult(String metricName, double score, double threshold, String feedback) {
        this.metricName = metricName == null ? "" : metricName;
        this.score = score;
        this.threshold = threshold;
        this.feedback = feedback == null ? "" : feedback;
        this.passed = score >= threshold;
    }

    public String metricName() {
        return metricName;
    }

    public double score() {
        return score;
    }

    public double threshold() {
        return threshold;
    }

    public String feedback() {
        return feedback;
    }

    public boolean passed() {
        return passed;
    }

    @Override
    public String toString() {
        return "EvalResult{metricName='" + metricName + "', score=" + score
            + ", threshold=" + threshold + ", passed=" + passed + ", feedback='" + feedback + "'}";
    }
}