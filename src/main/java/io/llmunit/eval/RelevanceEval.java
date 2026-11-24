package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Eval that scores how relevant the response is to the query and the provided grounding context.
 * The judge prompt and verdict are reported in the result feedback.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/evaluation/RelevancyEvaluator.html">RelevancyEvaluator</a>
 */
public class RelevanceEval extends SpringAIEval {

    public RelevanceEval(ChatClient.Builder builder) {
        this(builder, 0.5);
    }

    public RelevanceEval(ChatClient.Builder builder, double threshold) {
        super("relevance", threshold, new RelevanceJudgeEvaluator(builder));
    }
}