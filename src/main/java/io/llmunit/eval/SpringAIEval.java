package io.llmunit.eval;

import io.llmunit.core.AbstractEval;
import io.llmunit.core.Eval;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/**
 * Base for every Spring AI backed eval: wraps a Spring AI {@link Evaluator} as a core {@link Eval}
 * and converts the core's plain-text context into the Spring AI {@link Document}s the evaluator
 * expects. This is the single place the library touches Spring AI evaluation machinery.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/Evaluator.html">Evaluator</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationRequest.html">EvaluationRequest</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationResponse.html">EvaluationResponse</a>
 */
public abstract class SpringAIEval extends AbstractEval {

    private final Evaluator evaluator;

    protected SpringAIEval(String name, double threshold, Evaluator evaluator) {
        super(name, threshold);
        this.evaluator = evaluator;
    }

    @Override
    public EvalResult evaluate(EvalInput input) {
        EvaluationRequest request = new EvaluationRequest(input.query(), toDocuments(input.context()), input.output());
        EvaluationResponse response = evaluator.evaluate(request);
        return new EvalResult(name(), response.getScore(), threshold(), response.getFeedback());
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