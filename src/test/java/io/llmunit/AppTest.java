package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.core.LLMAssert;
import io.llmunit.eval.FactCheckingEval;
import io.llmunit.extension.LLMTestExtension;
import io.llmunit.support.StubChatModel;
import java.util.List;

/**
 * Offline smoke test verifying that an `@LLMTest` replays recorded evaluations without a live model.
 */
public class AppTest {

    @LLMTest(offline = true)
    void offlineTestReplaysWithoutLLM() {
        EvalInput input = new EvalInput("q", "a", List.of());
        LLMTestExtension.recorder().store().record(
            "fact_checking", input, new EvalResult("fact_checking", 1.0, 0.5, "ok"));

        LLMAssert.assertThatLLM("a")
            .withQuery("q")
            .passesEval(new FactCheckingEval(new StubChatModel("irrelevant").chatClientBuilder()));
    }
}