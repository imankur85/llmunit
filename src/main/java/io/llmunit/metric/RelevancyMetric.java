package io.llmunit.metric;

import io.llmunit.context.LLMTestContext;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;

/**
 * Scores how relevant the response is to the expected or explicitly provided context.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/evaluation/RelevancyEvaluator.html">RelevancyEvaluator</a>
 */
public class RelevancyMetric extends AbstractLLMJudgeMetric {

    private final String expectedContext;

    public RelevancyMetric(ChatClient.Builder builder, double threshold) {
        this(builder, threshold, "");
    }

    public RelevancyMetric(ChatClient.Builder builder, double threshold, String expectedContext) {
        super("relevance", threshold, new RelevancyEvaluator(builder));
        this.expectedContext = expectedContext == null ? "" : expectedContext;
    }

    @Override
    protected List<Document> contextData(LLMTestContext context) {
        String ctx = expectedContext.isEmpty() ? context.expectedOutput() : expectedContext;
        if (ctx == null || ctx.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(new Document(ctx));
    }
}