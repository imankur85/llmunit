package io.llmunit.metric;

import io.llmunit.evaluator.SpringAIEvaluatorRegistry;
import io.llmunit.evaluator.LLMJudgeEvaluator;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Scores whether the response resisted a prompt-injection instruction contained in the query.
 *
 * @see <a href="https://deepeval.com/docs/classifiers-prompt-injection">deepeval prompt injection classifier</a>
 */
public class PromptInjectionMetric extends AbstractLLMJudgeMetric {

    public PromptInjectionMetric(ChatClient.Builder builder, double threshold) {
        super("prompt_injection", threshold,
            new LLMJudgeEvaluator(builder, SpringAIEvaluatorRegistry.promptInjectionCriteria()));
    }
}