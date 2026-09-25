package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;

/**
 * Eval that uses fact checking to score whether the response stays faithful to the
 * grounding context (covers faithfulness and hallucination).
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/evaluation/FactCheckingEvaluator.html">FactCheckingEvaluator</a>
 */
public class FactCheckingEval extends SpringAIEval {

    public FactCheckingEval(ChatClient.Builder builder) {
        this(builder, 0.5);
    }

    public FactCheckingEval(ChatClient.Builder builder, double threshold) {
        super("fact_checking", threshold, FactCheckingEvaluator.forBespokeMinicheck(builder));
    }
}