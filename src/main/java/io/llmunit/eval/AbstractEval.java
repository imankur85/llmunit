package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Base for {@link Eval} implementations that score responses with a ChatClient.
 */
public abstract class AbstractEval implements Eval {

    private final String name;
    private final double threshold;
    private final ChatClient.Builder builder;

    protected AbstractEval(String name, double threshold, ChatClient.Builder builder) {
        this.name = name == null ? "" : name;
        this.threshold = threshold;
        this.builder = builder;
    }

    protected ChatClient chatClient() {
        return builder.build();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public double threshold() {
        return threshold;
    }

    protected static String clean(String verdict) {
        return verdict == null ? "" : verdict.strip().lines().findFirst().orElse("");
    }
}