package io.llmunit;

import io.llmunit.eval.EvalInput;
import io.llmunit.eval.EvalResult;
import io.llmunit.mock.EvalResultStore;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvalResultStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripsThroughJson() throws IOException {
        EvalResultStore store = new EvalResultStore();
        EvalInput input = new EvalInput("q", "a", List.of(new Document("ctx1")));
        store.record("relevance", input, new EvalResult("relevance", 0.75, 0.5, "ok"));

        Path file = tempDir.resolve("records.json");
        store.save(file);

        EvalResultStore loaded = new EvalResultStore();
        loaded.loadIfAbsent(file);
        Optional<EvalResult> replayed = loaded.lookup(EvalResultStore.key("relevance", input));
        assertTrue(replayed.isPresent());
        assertEquals(0.75, replayed.get().score(), 1e-9);
        assertTrue(replayed.get().passed());
        assertEquals("ok", replayed.get().feedback());
    }

    @Test
    void missingFileLeavesStoreEmpty() throws IOException {
        EvalResultStore store = new EvalResultStore();
        store.loadIfAbsent(tempDir.resolve("absent.json"));
        assertEquals(0, store.size());
        assertFalse(store.records().containsKey("anything"));
    }
}