package io.llmunit.core;

import io.llmunit.extension.LLMTestExtension;
import io.llmunit.extension.LLMTestExtension.TrialRecorder;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent assertions for an LLM output, used inside an `@LLMTest` body.
 * <pre>{@code
 * assertThatLLM(answer)
 *     .withQuery(query)
 *     .withContext(documents)
 *     .passesEval(new RelevanceEval(judge))
 *     .passesEval(new ToxicityEval(judge));
 * }</pre>
 *
 * <p>Spring-free: grounding context is plain text.
 *
 * @see <a href="https://deepeval.com/docs/metrics-introduction">deepeval metrics</a>
 */
public final class LLMAssert {

    private final TrialRecorder recorder;
    private String query = "";
    private final String output;
    private final List<String> context = new ArrayList<>();

    private LLMAssert(TrialRecorder recorder, String output) {
        this.recorder = recorder;
        this.output = output == null ? "" : output;
    }

    /**
     * Starts an assertion chain for the given model output. Only usable inside a test body
     * that is running under `@LLMTest`.
     */
    public static LLMAssert assertThatLLM(String output) {
        TrialRecorder recorder = LLMTestExtension.recorder();
        if (recorder == null) {
            throw new IllegalStateException(
                "assertThatLLM can only be used inside an @LLMTest method (offline or online).");
        }
        return new LLMAssert(recorder, output);
    }

    public LLMAssert withQuery(String query) {
        this.query = query == null ? "" : query;
        return this;
    }

    public LLMAssert withContext(List<String> texts) {
        if (texts != null) {
            context.addAll(texts);
        }
        return this;
    }

    public LLMAssert withContext(String... texts) {
        if (texts != null) {
            context.addAll(List.of(texts));
        }
        return this;
    }

    public LLMAssert passesEval(Eval eval) {
        return passesEval(eval, eval.threshold());
    }

    /**
     * Evaluates the output against the given eval and records the result, failing the
     * evaluation assertion if the score is below the threshold.
     */
    public LLMAssert passesEval(Eval eval, double threshold) {
        EvalInput input = new EvalInput(query, output, context);
        String key = EvalResultStore.key(eval.name(), input);

        EvalResult result;
        if (recorder.offline()) {
            result = recorder.store().lookup(key)
                .orElseGet(() -> new EvalResult(eval.name(), 0.0, threshold,
                    "llmunit: no recorded result for offline evaluation; run online or seed the store"));
        } else {
            EvalResult raw = eval.evaluate(input);
            result = new EvalResult(raw.metricName(), raw.score(), threshold, raw.feedback());
            recorder.store().record(eval.name(), input, result);
        }
        recorder.add(result);
        return this;
    }
}