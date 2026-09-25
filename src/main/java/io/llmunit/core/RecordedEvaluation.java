package io.llmunit.core;

import java.util.List;

/**
 * Immutable golden record of one evaluation: the input it was produced from plus its result.
 * Persisted to JSON for offline replay across test runs.
 */
public record RecordedEvaluation(
    String name,
    String query,
    String output,
    List<String> context,
    double score,
    double threshold,
    String feedback) {

    public EvalResult toResult() {
        return new EvalResult(name, score, threshold, feedback);
    }
}