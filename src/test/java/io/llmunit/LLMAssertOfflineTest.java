package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.core.EvalResultStore;
import io.llmunit.core.LLMAssert;
import io.llmunit.eval.RelevanceEval;
import io.llmunit.extension.LLMTestExtension;
import io.llmunit.support.StubChatModel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMAssertOfflineTest {

    private static final AtomicInteger TRIAL = new AtomicInteger();

    @LLMTest(offline = true)
    void seedAndReplayWithoutModel() {
        EvalInput input = new EvalInput("q1", "a", List.of());
        LLMTestExtension.recorder().store().record(
            "relevance", input, new EvalResult("relevance", 0.9, 0.5, "ok"));

        LLMAssert.assertThatLLM("a")
            .withQuery("q1")
            .passesEval(new RelevanceEval(new StubChatModel("irrelevant").chatClientBuilder()));

        EvalResult replayed = LLMTestExtension.recorder().results().get(0);
        assertTrue(replayed.passed());
        assertEquals(0.9, replayed.score(), 1e-9);
    }

    @LLMTest(trials = 2, passRate = 0.5, offline = true)
    void missingRecordingFailsOnlyThatTrial() {
        int i = TRIAL.getAndIncrement();
        if (i == 0) {
            EvalInput input = new EvalInput("q2", "a", List.of());
            LLMTestExtension.recorder().store().record(
                "relevance", input, new EvalResult("relevance", 0.9, 0.5, "ok"));
        }

        String query = i == 0 ? "q2" : "q3";
        LLMAssert.assertThatLLM("a")
            .withQuery(query)
            .passesEval(new RelevanceEval(new StubChatModel("irrelevant").chatClientBuilder()));

        EvalResult result = LLMTestExtension.recorder().results().get(0);
        if (i == 1) {
            assertFalse(result.passed());
            assertTrue(result.feedback().contains("no recorded result"));
        } else {
            assertTrue(result.passed());
        }
    }
}