package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.assertion.LLMAssert;
import io.llmunit.eval.ToxicityEval;
import io.llmunit.mock.EvalResultStore;
import io.llmunit.support.StubChatModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A recording run (system property <code>llmunit.recordGolden=true</code>) persists evaluation
 * results to the JSON records file that offline tests later replay.
 */
class RecordGoldenTest {

    private static final String RECORDS_DIR = "target/llmunit-records-test";

    @BeforeAll
    static void enableRecording() {
        System.setProperty("llmunit.recordsDir", RECORDS_DIR);
        System.setProperty("llmunit.recordGolden", "true");
    }

    @LLMTest(trials = 2, passRate = 1.0)
    void recordsResultsToFile() {
        LLMAssert.assertThatLLM("answer")
            .withQuery("question")
            .passesEval(new ToxicityEval(new StubChatModel("0.1").chatClientBuilder()));
    }

    @AfterAll
    static void verifyAndCleanup() throws IOException {
        try {
            Path file = Path.of(RECORDS_DIR, RecordGoldenTest.class.getSimpleName() + ".json");
            assertTrue(Files.isRegularFile(file), "expected records file " + file);
            EvalResultStore loaded = new EvalResultStore();
            loaded.loadIfAbsent(file);
            assertFalse(loaded.records().isEmpty());
        } finally {
            System.clearProperty("llmunit.recordGolden");
        }
    }
}