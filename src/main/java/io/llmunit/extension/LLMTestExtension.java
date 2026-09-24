package io.llmunit.extension;

import io.llmunit.annotations.LLMTest;
import io.llmunit.eval.EvalResult;
import io.llmunit.mock.EvalResultStore;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.opentest4j.AssertionFailedError;

/**
 * JUnit Jupiter extension backing {@code @LLMTest}. Each `@LLMTest` method runs as a test
 * template with `trials` invocations; a trial passes when every asserted eval passes. The test
 * fails only if fewer than `passRate` of the trials passed. In offline mode results are replayed
 * from the per-test-class {@link EvalResultStore} instead of calling a judge model.
 *
 * @see <a href="https://docs.spring.io/spring-ai/reference/api/testing.html">Spring AI testing</a>
 */
public class LLMTestExtension
    implements TestTemplateInvocationContextProvider, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(LLMTestExtension.class);
    private static final String STORE_KEY = "llmunit.evals";
    private static final String STATS_PREFIX = "llmunit.stats.";

    private static final ThreadLocal<TrialRecorder> RECORDER = new ThreadLocal<>();

    /** The recorder for the trial currently executing, or {@code null} outside an {@code @LLMTest} method. */
    public static TrialRecorder recorder() {
        return RECORDER.get();
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(LLMTestFilter::isLLMTest).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        LLMTest annotation = method.getAnnotation(LLMTest.class);
        int trials = Math.max(1, annotation.trials());
        return IntStream.range(0, trials).mapToObj(i -> new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return "trial " + (invocationIndex + 1);
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return List.of();
            }
        });
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        if (!LLMTestFilter.isLLMTest(method)) {
            return;
        }
        LLMTest annotation = method.getAnnotation(LLMTest.class);
        RECORDER.set(new TrialRecorder(store(context), annotation.offline()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        if (!LLMTestFilter.isLLMTest(method)) {
            return;
        }
        TrialRecorder recorder = RECORDER.get();
        RECORDER.remove();

        TrialStats stats = stats(context);
        if (recorder != null && recorder.allPassed()) {
            stats.markPass();
        }
        if (recorder != null && !recorder.allPassed()) {
            recorder.results().stream()
                .filter(r -> !r.passed())
                .forEach(r -> stats.recordFailure(r.feedback()));
        }
        stats.markDone();

        if (stats.done() && stats.passes() < stats.required()) {
            LLMTest annotation = method.getAnnotation(LLMTest.class);
            throw new AssertionFailedError(
                "LLM test '" + method.getName() + "' passed " + stats.passes() + "/" + stats.trials()
                    + " trials (required " + stats.required() + " at passRate " + annotation.passRate()
                    + "): " + stats.firstFailure());
        }
    }

    private static EvalResultStore store(ExtensionContext context) {
        ExtensionContext classContext = context.getParent().orElse(context);
        return classContext.getStore(NAMESPACE)
            .getOrComputeIfAbsent(STORE_KEY, k -> new EvalResultStore(), EvalResultStore.class);
    }

    private static TrialStats stats(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        ExtensionContext classContext = context.getParent().orElse(context);
        String key = STATS_PREFIX + method.toString();
        return classContext.getStore(NAMESPACE)
            .getOrComputeIfAbsent(key, k -> new TrialStats(method.getAnnotation(LLMTest.class)), TrialStats.class);
    }

    /**
     * Per-invocation collector: buffers the evaluation results asserted inside a trial and
     * exposes the per-test-class {@link EvalResultStore} plus the offline flag.
     */
    public static final class TrialRecorder {

        private final EvalResultStore store;
        private final boolean offline;
        private final List<EvalResult> results = new java.util.ArrayList<>();

        TrialRecorder(EvalResultStore store, boolean offline) {
            this.store = store;
            this.offline = offline;
        }

        public EvalResultStore store() {
            return store;
        }

        public boolean offline() {
            return offline;
        }

        public void add(EvalResult result) {
            results.add(result);
        }

        public boolean allPassed() {
            if (results.isEmpty()) {
                return true;
            }
            return results.stream().allMatch(EvalResult::passed);
        }

        public List<EvalResult> results() {
            return List.copyOf(results);
        }
    }

    private static final class TrialStats {

        private final int trials;
        private final int required;
        private int passes;
        private int done;
        private String firstFailure = "";

        TrialStats(LLMTest annotation) {
            this.trials = Math.max(1, annotation.trials());
            double passRate = Math.max(0.0, Math.min(1.0, annotation.passRate()));
            this.required = (int) Math.ceil(passRate * trials);
        }

        void markPass() {
            passes++;
        }

        void recordFailure(String feedback) {
            if (firstFailure.isBlank() && feedback != null && !feedback.isBlank()) {
                firstFailure = feedback;
            }
        }

        void markDone() {
            done++;
        }

        int passes() {
            return passes;
        }

        int trials() {
            return trials;
        }

        int required() {
            return required;
        }

        boolean done() {
            return done >= trials;
        }

        String firstFailure() {
            return firstFailure.isBlank() ? "no evaluation passed" : firstFailure;
        }
    }
}