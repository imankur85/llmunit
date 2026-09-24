package io.llmunit.metric;

import io.llmunit.context.EvaluationResult;
import io.llmunit.context.LLMTestContext;

/**
 * Contract for a named evaluation metric: given a test context it returns an evaluation result.
 */
public interface LLMMetric {

    String name();

    EvaluationResult evaluate(LLMTestContext context);
}