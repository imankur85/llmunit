package io.llmunit.metric;

import io.llmunit.context.EvaluationResult;
import io.llmunit.context.LLMTestContext;
import io.llmunit.evaluator.EvaluatorBridge;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.Evaluator;

public abstract class AbstractLLMJudgeMetric implements LLMMetric {

    private final String name;
    private final double threshold;
    private final Evaluator evaluator;

    protected AbstractLLMJudgeMetric(String name, double threshold, Evaluator evaluator) {
        this.name = name;
        this.threshold = threshold;
        this.evaluator = evaluator;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public EvaluationResult evaluate(LLMTestContext context) {
        return EvaluatorBridge.evaluate(evaluator, userText(context), contextData(context),
            response(context), threshold, name);
    }

    protected String userText(LLMTestContext context) {
        return context.input();
    }

    protected String response(LLMTestContext context) {
        return String.valueOf(context.actualOutput());
    }

    protected List<Document> contextData(LLMTestContext context) {
        return Collections.emptyList();
    }
}