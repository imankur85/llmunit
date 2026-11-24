package io.llmunit.eval;

import io.llmunit.core.Eval;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/**
 * Base for every Spring AI backed eval: holds the metric name and pass threshold, wraps a Spring AI
 * {@link Evaluator} as a core {@link Eval}, and converts the core's plain-text context into the Spring
 * AI {@link Document}s the evaluator expects. This is the single place the library touches Spring AI
 * evaluation machinery.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/Evaluator.html">Evaluator</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationRequest.html">EvaluationRequest</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationResponse.html">EvaluationResponse</a>
 */
public abstract class SpringAIEval implements Eval {

    static final String JUDGE_REQUEST = "llmunit.judgeRequest";
    static final String JUDGE_RESPONSE = "llmunit.judgeResponse";

    private final String name;
    private final double threshold;
    private final Evaluator evaluator;

    protected SpringAIEval(String name, double threshold, Evaluator evaluator) {
        this.name = name == null ? "" : name;
        this.threshold = threshold;
        this.evaluator = evaluator;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public double threshold() {
        return threshold;
    }

    @Override
    public EvalResult evaluate(EvalInput input) {
        EvaluationRequest request = new EvaluationRequest(input.query(), toDocuments(input.context()), input.output());
        EvaluationResponse response = evaluator.evaluate(request);
        return new EvalResult(name(), response.getScore(), threshold(), evaluationDetails(request, response));
    }

    static Map<String, Object> judgeExchange(String request, String response) {
        return Map.of(JUDGE_REQUEST, request == null ? "" : request,
            JUDGE_RESPONSE, response == null ? "" : response);
    }

    private String evaluationDetails(EvaluationRequest request, EvaluationResponse response) {
        String line = System.lineSeparator();
        StringBuilder details = new StringBuilder("llmunit: ").append(name()).append(" evaluation");
        String judgeRequest = metadata(response, JUDGE_REQUEST);
        String judgeResponse = metadata(response, JUDGE_RESPONSE);
        if (!judgeRequest.isBlank() || !judgeResponse.isBlank()) {
            details.append(line).append("judge request:").append(line).append(judgeRequest)
                .append(line).append("judge response:").append(line).append(judgeResponse);
        } else if (response.getFeedback() != null && !response.getFeedback().isBlank()) {
            details.append(line).append("judge feedback: ").append(response.getFeedback().strip());
        }
        return details.append(line).append("query: ").append(text(request.getUserText()))
            .append(line).append("output: ").append(text(request.getResponseContent()))
            .append(line).append("context: ").append(evaluator.doGetSupportingData(request))
            .toString();
    }

    private static String metadata(EvaluationResponse response, String key) {
        Object value = response.getMetadata() == null ? null : response.getMetadata().get(key);
        return value == null ? "" : value.toString();
    }

    private static String text(String value) {
        return value == null ? "" : value;
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