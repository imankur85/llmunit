package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

/**
 * Base for evals that delegate scoring to a Spring AI {@link Evaluator}, feeding it the
 * query, grounding context, and generated output.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/Evaluator.html">Evaluator</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationRequest.html">EvaluationRequest</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/evaluation/EvaluationResponse.html">EvaluationResponse</a>
 */
public abstract class SpringAIEval extends AbstractEval {

    private final Evaluator evaluator;

    protected SpringAIEval(String name, double threshold, ChatClient.Builder builder, Evaluator evaluator) {
        super(name, threshold, builder);
        this.evaluator = evaluator;
    }

    @Override
    public EvalResult evaluate(EvalInput input) {
        EvaluationRequest request = new EvaluationRequest(input.query(), input.context(), input.output());
        EvaluationResponse response = evaluator.evaluate(request);
        return new EvalResult(name(), response.getScore(), threshold(), response.getFeedback());
    }
}