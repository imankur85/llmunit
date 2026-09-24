package io.llmunit.metric;

import io.llmunit.context.LLMTestContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;

/**
 * Scores whether the response stays faithful to the expected answer (fact-checking metric).
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/evaluation/FactCheckingEvaluator.html">FactCheckingEvaluator</a>
 */
public class FaithfulnessMetric extends AbstractLLMJudgeMetric {

    public FaithfulnessMetric(ChatClient.Builder builder, double threshold) {
        super("faithfulness", threshold, FactCheckingEvaluator.forBespokeMinicheck(builder));
    }

    @Override
    protected String userText(LLMTestContext context) {
        return context.expectedOutput();
    }
}