package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;

/**
 * Eval that scores how relevant the response is to the query and the provided grounding context.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/evaluation/RelevancyEvaluator.html">RelevancyEvaluator</a>
 */
public class RelevanceEval extends SpringAIEval {

    public RelevanceEval(ChatClient.Builder builder) {
        this(builder, 0.5);
    }

    public RelevanceEval(ChatClient.Builder builder, double threshold) {
        super("relevance", threshold, builder, new RelevancyEvaluator(builder));
    }
}