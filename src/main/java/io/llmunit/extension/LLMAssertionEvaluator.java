package io.llmunit.extension;

import io.llmunit.context.EvaluationResult;
import io.llmunit.context.LLMTestContext;
import io.llmunit.metric.LLMMetric;
import io.llmunit.metric.MetricFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Turns a test method's assertion and `@Metric` annotations into metrics and runs them,
 * collecting the resulting evaluation results.
 */
public class LLMAssertionEvaluator {

    private final MetricFactory metricFactory;

    public LLMAssertionEvaluator(ChatClient.Builder chatClientBuilder) {
        this.metricFactory = new MetricFactory(chatClientBuilder);
    }

    public List<EvaluationResult> evaluate(LLMTestContext context) {
        List<LLMMetric> metrics = metricFactory.fromMethod(context.testMethod());
        return metrics.stream()
            .map(metric -> metric.evaluate(context))
            .collect(Collectors.toList());
    }
}