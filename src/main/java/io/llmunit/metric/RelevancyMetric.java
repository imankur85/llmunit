package io.llmunit.metric;

import io.llmunit.context.LLMTestContext;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;

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