package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.extension.LLMTestExtension;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppTest {

    @BeforeAll
    static void setUp() {
        LLMTestExtension.registerInput("question", "What is 2+2?");
    }

    @LLMTest(
        prompt = "You are a helpful assistant. Answer the following question: {{question}}",
        expected = "You are a helpful assistant. Answer the following question: {{question}}",
        offline = true
    )
    void offlineTestRunsWithoutLLM(String question) {
        assertNotNull(question);
    }
}