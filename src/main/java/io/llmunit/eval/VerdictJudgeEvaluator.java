package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

abstract class VerdictJudgeEvaluator implements Evaluator {

    private final ChatClient.Builder builder;

    VerdictJudgeEvaluator(ChatClient.Builder builder) {
        this.builder = builder;
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest request) {
        Prompt prompt = prompt(request);
        String verdict = builder.build().prompt(prompt).call()
            .chatClientResponse().chatResponse().getResult().getOutput().getText();
        String normalized = verdict == null ? "" : verdict.strip();
        boolean passing = passing(normalized);
        return new EvaluationResponse(passing, passing ? 1.0f : 0.0f, normalized,
            SpringAIEval.judgeExchange(prompt.getContents(), normalized));
    }

    protected abstract Prompt prompt(EvaluationRequest request);

    protected abstract boolean passing(String verdict);

    protected static String text(String value) {
        return value == null ? "" : value;
    }
}
