package io.llmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Asserts that the LLM response is relevant to the expected context of the test method.
 *
 * @see <a href="https://deepeval.com/docs/metrics-answer-relevancy">deepeval answer relevancy</a>
 */
public @interface AssertRelevance {
    double threshold() default 0.7;
    String expectedContext() default "";
}
