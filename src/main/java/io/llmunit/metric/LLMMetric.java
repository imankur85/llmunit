package io.llmunit.metric;

import io.llmunit.context.EvaluationResult;
import io.llmunit.context.LLMTestContext;

public interface LLMMetric {

    String name();

    EvaluationResult evaluate(LLMTestContext context);
}