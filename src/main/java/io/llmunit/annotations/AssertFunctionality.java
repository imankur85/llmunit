package io.llmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Asserts that the LLM response fulfills the expected functionality for the test method.
 *
 * @see <a href="https://deepeval.com/docs/metrics-introduction">deepeval metrics</a>
 */
public @interface AssertFunctionality {

    String expected() default "";

    double threshold() default 0.7;
}