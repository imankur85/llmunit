package io.llmunit;

import io.llmunit.core.BiasEval;
import io.llmunit.core.Eval;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.core.Judge;
import io.llmunit.core.PromptInjectionEval;
import io.llmunit.core.ToxicityEval;
import io.llmunit.eval.SpringAIEval;
import io.llmunit.support.StubChatModel;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JudgeEvalTest {

    private static final EvalInput INPUT = new EvalInput("q", "a", List.of());

    private static Judge judge(StubChatModel model) {
        return SpringAIEval.judge(model.chatClientBuilder());
    }

    @Test
    void safeResponseScoresHighQuality() {
        Eval eval = new ToxicityEval(judge(new StubChatModel("0.1")));
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(0.9, result.score(), 1e-9);
    }

    @Test
    void toxicResponseFailsDefaultThreshold() {
        Eval eval = new ToxicityEval(judge(new StubChatModel("0.9")));
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.1, result.score(), 1e-9);
    }

    @Test
    void customThresholdIsApplied() {
        Eval eval = new ToxicityEval(judge(new StubChatModel("0.2")), 0.9);
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.8, result.score(), 1e-9);
        assertEquals(0.9, result.threshold(), 1e-9);
    }

    @Test
    void promptInjectionResistedScoresHigh() {
        Eval eval = new PromptInjectionEval(judge(new StubChatModel("0.2")));
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(0.8, result.score(), 1e-9);
    }

    @Test
    void biasedResponseFails() {
        Eval eval = new BiasEval(judge(new StubChatModel("1.0")));
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.0, result.score(), 1e-9);
    }

    @Test
    void verdictEmbeddedInTextParsed() {
        Eval eval = new BiasEval(judge(new StubChatModel("The score is 0.75 out of 1")));
        EvalResult result = eval.evaluate(INPUT);
        assertEquals(0.25, result.score(), 1e-9);
    }
}