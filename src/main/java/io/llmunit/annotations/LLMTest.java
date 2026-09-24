package io.llmunit.annotations;

import io.llmunit.extension.LLMTestExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@ExtendWith(LLMTestExtension.class)
/**
 * Marks a method as an LLM unit test. Meta-annotated with `@Test` and
 * `@ExtendWith(LLMTestExtension.class)`, so declaring it replaces the plain JUnit `@Test`.
 *
 * @see <a href="https://docs.spring.io/spring-ai/reference/api/testing.html">Spring AI testing</a>
 */
public @interface LLMTest {

    String prompt();

    String expected() default "";

    String model() default "llama3.1:8b";

    boolean offline() default false;
}