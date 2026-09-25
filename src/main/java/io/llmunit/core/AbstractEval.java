package io.llmunit.core;

/**
 * Base for {@link Eval} implementations, holding the metric name and pass threshold.
 * Spring-free.
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
}