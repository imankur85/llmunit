package io.llmunit;

import io.llmunit.eval.BiasEval;
import io.llmunit.eval.Eval;
import io.llmunit.eval.EvalInput;
import io.llmunit.eval.EvalResult;
import io.llmunit.eval.PromptInjectionEval;
import io.llmunit.eval.ToxicityEval;
import io.llmunit.support.StubChatModel;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JudgeEvalTest {

    private static final EvalInput INPUT = new EvalInput("q", "a", List.of());

    @Test
    void safeResponseScoresHighQuality() {
        Eval eval = new ToxicityEval(new StubChatModel("0.1").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(0.9, result.score(), 1e-9);
    }

    @Test
    void toxicResponseFailsDefaultThreshold() {
        Eval eval = new ToxicityEval(new StubChatModel("0.9").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.1, result.score(), 1e-9);
    }

    @Test
    void customThresholdIsApplied() {
        Eval eval = new ToxicityEval(new StubChatModel("0.2").chatClientBuilder(), 0.9);
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.8, result.score(), 1e-9);
        assertEquals(0.9, result.threshold(), 1e-9);
    }

    @Test
    void promptInjectionResistedScoresHigh() {
        Eval eval = new PromptInjectionEval(new StubChatModel("0.2").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(0.8, result.score(), 1e-9);
    }

    @Test
    void biasedResponseFails() {
        Eval eval = new BiasEval(new StubChatModel("1.0").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.0, result.score(), 1e-9);
    }

    @Test
    void verdictEmbeddedInTextParsed() {
        Eval eval = new BiasEval(new StubChatModel("The score is 0.75 out of 1").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertEquals(0.25, result.score(), 1e-9);
    }
}