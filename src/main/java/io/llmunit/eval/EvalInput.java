package io.llmunit.eval;

import java.util.List;
import org.springframework.ai.document.Document;

/**
 * The input to an {@link Eval}: the original query, the system's output, and any
 * grounding context the app used to produce it.
 */
public final class EvalInput {

    private final String query;
    private final String output;
    private final List<Document> context;

    public EvalInput(String query, String output, List<Document> context) {
        this.query = query == null ? "" : query;
        this.output = output == null ? "" : output;
        this.context = context == null ? List.of() : List.copyOf(context);
    }

    public String query() {
        return query;
    }

    public String output() {
        return output;
    }

    public List<Document> context() {
        return context;
    }
}