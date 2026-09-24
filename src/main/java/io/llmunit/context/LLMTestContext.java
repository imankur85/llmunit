package io.llmunit.context;

import io.llmunit.annotations.LLMTest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable per-invocation state for a single `@LLMTest`: the test method, its annotation,
 * resolved parameters, input/expected/actual output, and the collected evaluation results.
 */
public class LLMTestContext {

    private final Method testMethod;
    private final LLMTest annotation;
    private final TestParameters parameters = new TestParameters();
    private final List<EvaluationResult> results = new ArrayList<>();
    private String input = "";
    private String expectedOutput = "";
    private String actualOutput;

    public LLMTestContext(Method testMethod, LLMTest annotation) {
        this.testMethod = testMethod;
        this.annotation = annotation;
    }

    public Method testMethod() {
        return testMethod;
    }

    public LLMTest annotation() {
        return annotation;
    }

    public TestParameters parameters() {
        return parameters;
    }

    public String input() {
        return input;
    }

    public void setInput(String input) {
        this.input = input == null ? "" : input;
    }

    public String expectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput == null ? "" : expectedOutput;
    }

    public String actualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public List<EvaluationResult> results() {
        return Collections.unmodifiableList(results);
    }

    public void addResult(EvaluationResult result) {
        results.add(result);
    }

    public void addResults(List<EvaluationResult> newResults) {
        results.addAll(newResults);
    }
}