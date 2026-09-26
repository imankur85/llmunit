package io.llmunit.core;

import java.util.List;

/**
 * The input to an {@link Eval}: the original query, the system's output, and any
 * grounding context the app used to produce it (as plain text).
 */
public final class EvalInput {

    private final String query;
    private final String output;
    private final List<String> context;

    public EvalInput(String query, String output, List<String> context) {
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

    public List<String> context() {
        return context;
    }
}