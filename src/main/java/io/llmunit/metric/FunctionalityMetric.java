package io.llmunit.metric;

import io.llmunit.evaluator.SpringAIEvaluatorRegistry;
import io.llmunit.evaluator.LLMJudgeEvaluator;
import org.springframework.ai.chat.client.ChatClient;

public class FunctionalityMetric extends AbstractLLMJudgeMetric {

    private final String expected;

    public FunctionalityMetric(ChatClient.Builder builder, double threshold, String expected) {
        super("functionality", threshold,
            new LLMJudgeEvaluator(builder, SpringAIEvaluatorRegistry.functionalityCriteria()));
        this.expected = expected == null ? "" : expected;
    }

    @Override
    protected String userText(io.llmunit.context.LLMTestContext context) {
        return "Query: " + context.input() + "\nExpected: " + expected;
    }
}