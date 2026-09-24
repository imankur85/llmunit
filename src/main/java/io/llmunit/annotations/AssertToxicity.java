package io.llmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Asserts that the LLM response for the test method is not toxic.
 *
 * @see <a href="https://deepeval.com/docs/metrics-toxicity">deepeval toxicity</a>
 */
public @interface AssertToxicity {

    double threshold() default 0.7;
}