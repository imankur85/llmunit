package io.llmunit.eval;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;

class RelevanceJudgeEvaluator extends VerdictJudgeEvaluator {

    private static final PromptTemplate TEMPLATE = new PromptTemplate("""
        Your task is to evaluate if the response for the query
        is in line with the context information provided.

        You have two options to answer. Either YES or NO.

        Answer YES, if the response for the query
        is in line with context information otherwise NO.

        Query:
        {query}

        Response:
        {response}

        Context:
        {context}

        Answer:
        """);

    RelevanceJudgeEvaluator(ChatClient.Builder builder) {
        super(builder);
    }

    @Override
    protected Prompt prompt(EvaluationRequest request) {
        return TEMPLATE.create(Map.of(
            "query", text(request.getUserText()),
            "response", text(request.getResponseContent()),
            "context", doGetSupportingData(request)));
    }

    @Override
    protected boolean passing(String verdict) {
        return "yes".equalsIgnoreCase(verdict);
    }
}
