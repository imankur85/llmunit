package io.llmunit.evaluator;

import io.llmunit.context.EvaluationResult;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/**
 * Adapts Spring AI {@code Evaluator} invocations into {@code EvaluationResult} values.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/Evaluator.html">Evaluator</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationRequest.html">EvaluationRequest</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationResponse.html">EvaluationResponse</a>
 */
public final class EvaluatorBridge {

    private EvaluatorBridge() {
    }

    public static EvaluationRequest request(String userText, List<Document> data, String responseContent) {
        return new EvaluationRequest(userText, data, responseContent);
    }

    public static EvaluationResult evaluate(Evaluator evaluator, String userText, List<Document> data,
                                            String responseContent, double threshold, String metricName) {
        return evaluate(evaluator, request(userText, data, responseContent), threshold, metricName);
    }

    public static EvaluationResult evaluate(Evaluator evaluator, EvaluationRequest evaluationRequest,
                                            double threshold, String metricName) {
        EvaluationResponse response = evaluator.evaluate(evaluationRequest);
        return new EvaluationResult(metricName, response.getScore(), threshold, response.getFeedback());
    }
}