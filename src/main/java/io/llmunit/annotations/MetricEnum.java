package io.llmunit.annotations;

/**
 * Enumeration of the evaluation metrics supported by `@Metric`.
 */
public enum MetricEnum {
    FAITHFULNESS,
    ANSWER_RELEVANCY,
    RELEVANCY,
    HALLUCINATION,
    TOXICITY,
    PROMPT_INJECTION,
    FUNCTIONALITY,
    BIAS,
    CONTEXTUAL_RELEVANCY
}