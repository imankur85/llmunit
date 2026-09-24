package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.assertion.LLMAssert;
import io.llmunit.eval.EvalInput;
import io.llmunit.eval.EvalResult;
import io.llmunit.eval.RelevanceEval;
import io.llmunit.extension.LLMTestExtension;
import io.llmunit.mock.EvalResultStore;
import io.llmunit.support.StubChatModel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.ai.document.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline mode replays golden records persisted to a JSON file by an earlier (recording) run,
 * verifying the record/replay mechanism across what would otherwise be separate test runs.
 */
class OfflineFileReplayTest {

    private static final String RECORDS_DIR = "target/llmunit-records-test";

    @BeforeAll
    static void writeGoldenRecords() throws IOException {
        System.setProperty("llmunit.recordsDir", RECORDS_DIR);
        EvalResultStore store = new EvalResultStore();
        EvalInput input = new EvalInput("flavor", "healthy drink", List.of(new Document("coke facts")));
        store.record("relevance", input, new EvalResult("relevance", 0.95, 0.5, "ok"));
        store.save(Path.of(RECORDS_DIR, OfflineFileReplayTest.class.getSimpleName() + ".json"));
    }

    @LLMTest(offline = true, trials = 2, passRate = 1.0)
    void replaysRecordedEvalFromFile() {
        LLMAssert.assertThatLLM("healthy drink")
            .withQuery("flavor")
            .withContext("coke facts")
            .passesEval(new RelevanceEval(new StubChatModel("irrelevant").chatClientBuilder()));

        EvalResult replayed = LLMTestExtension.recorder().results().get(0);
        assertTrue(replayed.passed());
        assertEquals(0.95, replayed.score(), 1e-9);
    }
}