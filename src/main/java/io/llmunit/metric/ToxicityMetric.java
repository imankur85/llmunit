package io.llmunit.metric;

import io.llmunit.evaluator.SpringAIEvaluatorRegistry;
import io.llmunit.evaluator.LLMJudgeEvaluator;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Scores whether the response is toxic (offensive, harmful, hateful, or unsafe content).
 *
 * @see <a href="https://deepeval.com/docs/metrics-toxicity">deepeval toxicity</a>
 */
public class ToxicityMetric extends AbstractLLMJudgeMetric {

    public ToxicityMetric(ChatClient.Builder builder, double threshold) {
        super("toxicity", threshold,
            new LLMJudgeEvaluator(builder, SpringAIEvaluatorRegistry.toxicityCriteria()));
    }
}