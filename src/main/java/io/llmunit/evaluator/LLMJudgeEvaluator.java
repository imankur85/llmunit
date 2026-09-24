package io.llmunit.evaluator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

public class LLMJudgeEvaluator implements Evaluator {

    private static final Pattern SCORE = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    private static final String VERDICT_TEMPLATE = "Query:%n%s%n%nResponse:%n%s%n%n"
        + "Return a single number between 0 and 1 (inclusive) representing how well the response "
        + "satisfies the given criteria. Respond only with the number.";

    private final ChatClient chatClient;
    private final String criteria;

    public LLMJudgeEvaluator(ChatClient.Builder builder, String criteria) {
        this.chatClient = builder.build();
        this.criteria = criteria;
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest request) {
        String userText = request.getUserText() == null ? "" : request.getUserText();
        String response = request.getResponseContent() == null ? "" : request.getResponseContent();

        String verdict = chatClient.prompt()
            .system(criteria)
            .user(String.format(VERDICT_TEMPLATE, userText, response))
            .call()
            .chatClientResponse()
            .chatResponse()
            .getResult()
            .getOutput()
            .getText();

        double score = parseScore(verdict);
        return new EvaluationResponse(score >= 0.5, (float) score, "Judge verdict: " + clean(verdict), Map.of());
    }

    private double parseScore(String verdict) {
        if (verdict == null) {
            return 0.0;
        }
        Matcher matcher = SCORE.matcher(verdict);
        if (matcher.find()) {
            try {
                return Math.max(0.0, Math.min(1.0, Double.parseDouble(matcher.group(1))));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String clean(String verdict) {
        return verdict == null ? "" : verdict.strip().lines().findFirst().orElse("");
    }
}