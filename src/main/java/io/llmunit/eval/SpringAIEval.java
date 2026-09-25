package io.llmunit.eval;

import io.llmunit.core.AbstractEval;
import io.llmunit.core.Eval;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.core.Judge;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/**
 * The single bridge between the Spring-free {@code io.llmunit.core} evals and Spring AI.
 * Wraps a Spring AI {@link Evaluator} as a core {@link Eval} and converts the core's plain-text
 * context to Spring AI {@link Document}s. Also provides {@link #judge(ChatClient.Builder)}, which
 * adapts a {@code ChatClient.Builder} to the core {@link Judge} abstraction used by guardrail
 * evals (toxicity, prompt-injection, bias).
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/Evaluator.html">Evaluator</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationRequest.html">EvaluationRequest</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationResponse.html">EvaluationResponse</a>
 */
public abstract class SpringAIEval extends AbstractEval {

    private final Evaluator evaluator;

    protected SpringAIEval(String name, double threshold, ChatClient.Builder builder, Evaluator evaluator) {
        super(name, threshold);
        this.evaluator = evaluator;
    }

    @Override
    public EvalResult evaluate(EvalInput input) {
        EvaluationRequest request = new EvaluationRequest(input.query(), toDocuments(input.context()), input.output());
        EvaluationResponse response = evaluator.evaluate(request);
        return new EvalResult(name(), response.getScore(), threshold(), response.getFeedback());
    }

    /**
     * Adapts a {@code ChatClient.Builder} to the core {@link Judge} interface: the prompt is sent
     * to the model and the model's text reply is returned as the verdict.
     */
    public static Judge judge(ChatClient.Builder builder) {
        return prompt -> {
            String text = builder.build().prompt()
                .user(prompt)
                .call()
                .chatClientResponse()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
            return text == null ? "" : text;
        };
    }

    /** Converts the core plain-text context into Spring AI {@link Document}s. */
    public static List<Document> toDocuments(List<String> texts) {
        return texts == null ? List.of() : texts.stream().map(Document::new).toList();
    }

    /** Converts Spring AI {@link Document}s into the core plain-text context. */
    public static List<String> toTexts(List<Document> documents) {
        return documents == null
            ? List.of()
            : documents.stream().map(doc -> doc.getText() == null ? "" : doc.getText()).toList();
    }
}