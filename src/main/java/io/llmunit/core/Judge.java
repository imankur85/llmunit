package io.llmunit.core;

/**
 * Text-in/text-out LLM judge used by guardrail evals. Implementations connect to a model of
 * the user's choosing; the framework itself is model-agnostic (the Spring AI bridge provides
 * a {@code Judge} backed by a {@code ChatClient.Builder}).
 *
 * <p>For an output that answers the given prompt with a single verdict (typically a number),
 * use in the {@code AbstractJudgeEval} subclasses.
 */
@FunctionalInterface
public interface Judge {

    String respond(String prompt);
}