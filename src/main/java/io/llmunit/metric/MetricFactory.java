package io.llmunit.metric;

import io.llmunit.annotations.AssertFunctionality;
import io.llmunit.annotations.AssertPromptInjection;
import io.llmunit.annotations.AssertRelevance;
import io.llmunit.annotations.AssertToxicity;
import io.llmunit.annotations.Metric;
import io.llmunit.annotations.MetricEnum;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;

public class MetricFactory {

    private final ChatClient.Builder chatClientBuilder;

    public MetricFactory(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public List<LLMMetric> fromMethod(Method testMethod) {
        List<LLMMetric> metrics = new ArrayList<>();

        AssertFunctionality functionality = testMethod.getAnnotation(AssertFunctionality.class);
        if (functionality != null) {
            metrics.add(new FunctionalityMetric(chatClientBuilder, functionality.threshold(), functionality.expected()));
        }

        AssertToxicity toxicity = testMethod.getAnnotation(AssertToxicity.class);
        if (toxicity != null) {
            metrics.add(new ToxicityMetric(chatClientBuilder, toxicity.threshold()));
        }

        AssertRelevance relevance = testMethod.getAnnotation(AssertRelevance.class);
        if (relevance != null) {
            metrics.add(new RelevancyMetric(chatClientBuilder, relevance.threshold(), relevance.expectedContext()));
        }

        AssertPromptInjection promptInjection = testMethod.getAnnotation(AssertPromptInjection.class);
        if (promptInjection != null) {
            metrics.add(new PromptInjectionMetric(chatClientBuilder, promptInjection.threshold()));
        }

        for (Metric annotation : testMethod.getAnnotationsByType(Metric.class)) {
            metrics.add(toMetric(annotation));
        }

        return metrics;
    }

    private LLMMetric toMetric(Metric annotation) {
        double threshold = annotation.threshold();
        switch (annotation.metric()) {
            case FAITHFULNESS:
            case HALLUCINATION:
                return new FaithfulnessMetric(chatClientBuilder, threshold);
            case RELEVANCY:
            case ANSWER_RELEVANCY:
            case CONTEXTUAL_RELEVANCY:
                return new RelevancyMetric(chatClientBuilder, threshold, annotation.key());
            case TOXICITY:
                return new ToxicityMetric(chatClientBuilder, threshold);
            case PROMPT_INJECTION:
                return new PromptInjectionMetric(chatClientBuilder, threshold);
            case FUNCTIONALITY:
                return new FunctionalityMetric(chatClientBuilder, threshold, annotation.key());
            case BIAS:
                throw new IllegalArgumentException("Metric BIAS is not supported yet");
            default:
                throw new IllegalArgumentException("Unsupported metric: " + annotation.metric());
        }
    }
}