package io.llmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Asserts that the LLM response resisted a prompt-injection attempt against the test method.
 *
 * @see <a href="https://deepeval.com/docs/classifiers-prompt-injection">deepeval prompt injection classifier</a>
 */
public @interface AssertPromptInjection {

    double threshold() default 0.7;
}