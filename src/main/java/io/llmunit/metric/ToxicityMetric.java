package io.llmunit.metric;

import io.llmunit.evaluator.SpringAIEvaluatorRegistry;
import io.llmunit.evaluator.LLMJudgeEvaluator;
import org.springframework.ai.chat.client.ChatClient;

public class ToxicityMetric extends AbstractLLMJudgeMetric {

    public ToxicityMetric(ChatClient.Builder builder, double threshold) {
        super("toxicity", threshold,
            new LLMJudgeEvaluator(builder, SpringAIEvaluatorRegistry.toxicityCriteria()));
    }
}