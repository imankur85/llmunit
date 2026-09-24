package io.llmunit.metric;

import io.llmunit.context.LLMTestContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;

public class FaithfulnessMetric extends AbstractLLMJudgeMetric {

    public FaithfulnessMetric(ChatClient.Builder builder, double threshold) {
        super("faithfulness", threshold, FactCheckingEvaluator.forBespokeMinicheck(builder));
    }

    @Override
    protected String userText(LLMTestContext context) {
        return context.expectedOutput();
    }
}