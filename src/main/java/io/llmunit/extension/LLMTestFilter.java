package io.llmunit.extension;

import io.llmunit.annotations.LLMTest;
import java.lang.reflect.Method;

/**
 * Predicates that gate extension behavior to `@LLMTest`-annotated test methods.
 */
public final class LLMTestFilter {

    private LLMTestFilter() {
    }

    public static boolean isLLMTest(Method method) {
        return method != null && method.isAnnotationPresent(LLMTest.class);
    }

    public static LLMTest annotation(Method method) {
        return method == null ? null : method.getAnnotation(LLMTest.class);
    }
}