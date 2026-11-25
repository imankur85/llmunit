package io.llmunit;

import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.eval.RelevanceEval;
import io.llmunit.support.StubChatModel;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelevanceEvalTest {

    private static final EvalInput INPUT = new EvalInput("q", "a", List.of("doc one", "doc two"));

    @Test
    void relevantResponsePasses() {
        RelevanceEval eval = new RelevanceEval(new StubChatModel(" YES ").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertTrue(result.passed());
        assertEquals(1.0, result.score(), 1e-6);
    }

    @Test
    void irrelevantResponseFailsWithJudgeExchange() {
        RelevanceEval eval = new RelevanceEval(new StubChatModel("no").chatClientBuilder());
        EvalResult result = eval.evaluate(INPUT);
        assertFalse(result.passed());
        assertEquals(0.0, result.score(), 1e-6);
        String line = System.lineSeparator();
        assertTrue(result.feedback().contains("judge request:"));
        assertTrue(result.feedback().contains("Query:" + line + "q"));
        assertTrue(result.feedback().contains("Response:" + line + "a"));
        assertTrue(result.feedback().contains("Context:" + line + "doc one" + line + "doc two"));
        assertTrue(result.feedback().contains("Answer:"));
        assertTrue(result.feedback().contains("judge response:" + line + "no"));
        assertTrue(result.feedback().contains("query: q"));
        assertTrue(result.feedback().contains("output: a"));
        assertTrue(result.feedback().contains("context: doc one"));
    }
}
