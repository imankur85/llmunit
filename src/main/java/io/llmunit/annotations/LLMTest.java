package io.llmunit.annotations;

import io.llmunit.extension.LLMTestExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Marks a method as an LLM evaluation test. Meta-annotated with `@TestTemplate` and
 * `@ExtendWith(LLMTestExtension.class)`. The test body calls the system under test and
 * asserts its output via {@code assertThatLLM(...)}.
 * <p>
 * `trials` and `passRate` handle non-deterministic model output: the body is executed
 * `trials` times and the test passes only if at least {@code ceil(passRate * trials)}
 * invocations pass all their evals.
 * <p>
 * With `offline=true` the judge evaluations are replayed from recorded results instead of
 * calling a live model.
 *
 * @see <a href="https://docs.spring.io/spring-ai/reference/api/testing.html">Spring AI testing</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(LLMTestExtension.class)
public @interface LLMTest {

    int trials() default 1;

    double passRate() default 1.0;

    boolean offline() default false;
}