package io.llmunit.core;

/**
 * Base for {@link Eval} implementations, holding the metric name and pass threshold.
 * Spring-free: model connectivity is delegated to subclasses or a {@link Judge}.
 */
public abstract class AbstractEval implements Eval {

    private final String name;
    private final double threshold;

    protected AbstractEval(String name, double threshold) {
        this.name = name == null ? "" : name;
        this.threshold = threshold;
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