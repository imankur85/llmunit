package io.llmunit.metric;

import io.llmunit.evaluator.SpringAIEvaluatorRegistry;
import io.llmunit.evaluator.LLMJudgeEvaluator;
import org.springframework.ai.chat.client.ChatClient;

public class PromptInjectionMetric extends AbstractLLMJudgeMetric {

    public PromptInjectionMetric(ChatClient.Builder builder, double threshold) {
        super("prompt_injection", threshold,
            new LLMJudgeEvaluator(builder, SpringAIEvaluatorRegistry.promptInjectionCriteria()));
    }
}