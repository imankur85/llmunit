package io.llmunit;

import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.eval.FactCheckingEval;
import io.llmunit.support.StubChatModel;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactCheckingEvalTest {

    private static final EvalInput INPUT = new EvalInput("q", "a", List.of("doc one", "doc two"));

    @Test
    void supportedClaimPasses() {
        FactCheckingEval eval = new FactCheckingEval(new StubChatModel("yes").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(1.0, result.score(), 1e-6);
    }

    @Test
    void unsupportedClaimFailsWithJudgeExchange() {
        FactCheckingEval eval = new FactCheckingEval(new StubChatModel("no").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.0, result.score(), 1e-6);
        assertTrue(result.feedback().contains("judge request:"));
        String line = System.lineSeparator();
        assertTrue(result.feedback().contains("Document:" + line + "doc one" + line + "doc two"));
        assertTrue(result.feedback().contains("Claim:" + line + "a"));
        assertTrue(result.feedback().contains("judge response:"));
        assertTrue(result.feedback().contains("no"));
        assertTrue(result.feedback().contains("query: q"));
        assertTrue(result.feedback().contains("output: a"));
        assertTrue(result.feedback().contains("context: doc one"));
    }
}
