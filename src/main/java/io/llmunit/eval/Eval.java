package io.llmunit.eval;

/**
 * An evaluator contract: maps an {@link EvalInput} (query, output, context) to an
 * {@link EvalResult} (passed, score, feedback). This is the whole evaluation contract.
 */
public interface Eval {

    String name();

    double threshold();

    EvalResult evaluate(EvalInput input);
}