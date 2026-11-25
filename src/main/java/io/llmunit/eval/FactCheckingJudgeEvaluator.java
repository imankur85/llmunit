package io.llmunit.eval;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;

class FactCheckingJudgeEvaluator extends VerdictJudgeEvaluator {

    private static final PromptTemplate TEMPLATE = new PromptTemplate("""
            Determine whether the claim is fully supported by the facts in the document.

            You have two options to answer. Either YES or NO.
            Answer YES if the claim is supported by the document, otherwise NO.

            Document:
            {document}

            Claim:
            {claim}

            Answer:
            """);

    FactCheckingJudgeEvaluator(ChatClient.Builder builder) {
        super(builder);
    }

    @Override
    protected Prompt prompt(EvaluationRequest request) {
        return TEMPLATE.create(Map.of(
                "document", doGetSupportingData(request),
                "claim", text(request.getResponseContent())));
    }

    @Override
    protected boolean passing(String verdict) {
        return "yes".equalsIgnoreCase(verdict);
    }
}
