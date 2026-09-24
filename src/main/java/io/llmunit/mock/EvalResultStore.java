package io.llmunit.mock;

import io.llmunit.eval.EvalInput;
import io.llmunit.eval.EvalResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-test-class store of recorded evaluation results, keyed by eval name and input.
 * Replayed verbatim in offline mode so tests run without contacting a judge model.
 *
 * <p>Records can be persisted to and loaded from a JSON file (see {@link #save(Path)} and
 * {@link #loadIfAbsent(Path)}), so golden results recorded with a live judge can be replayed
 * deterministically on later runs without a model.
 */
public class EvalResultStore {

    private final Map<String, RecordedEvaluation> results = new ConcurrentHashMap<>();
    private boolean loaded;

    public EvalResultStore record(String evalName, EvalInput input, EvalResult result) {
        RecordedEvaluation rec = new RecordedEvaluation(
            evalName, input.query(), input.output(), contextTexts(input),
            result.score(), result.threshold(), result.feedback());
        results.put(key(evalName, input), rec);
        return this;
    }

    public Optional<EvalResult> lookup(String key) {
        return Optional.ofNullable(results.get(key)).map(RecordedEvaluation::toResult);
    }

    public boolean has(String key) {
        return results.containsKey(key);
    }

    public int size() {
        return results.size();
    }

    /** Snapshot of the stored records, keyed like {@link #key(String, EvalInput)}. */
    public Map<String, RecordedEvaluation> records() {
        return Map.copyOf(results);
    }

    /**
     * Loads persisted records from the given JSON file. Runs at most once per store; a missing
     * file is ignored so replay degrades to "no recorded result" lookups.
     */
    public synchronized void loadIfAbsent(Path path) throws IOException {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.isRegularFile(path)) {
            return;
        }
        JsonMapper mapper = objectMapper();
        List<RecordedEvaluation> list =
            mapper.readValue(Files.readString(path), new TypeReference<List<RecordedEvaluation>>() {});
        for (RecordedEvaluation rec : list) {
            List<Document> docs = rec.context() == null
                ? List.of()
                : rec.context().stream().map(Document::new).toList();
            EvalInput input = new EvalInput(rec.query(), rec.output(), docs);
            results.put(key(rec.name(), input), rec);
        }
    }

    /** Writes all recorded evaluations as pretty JSON to the given file. Empty stores are left untouched. */
    public void save(Path path) throws IOException {
        if (results.isEmpty()) {
            return;
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        JsonMapper mapper = objectMapper();
        Files.writeString(path, mapper.writeValueAsString(results.values()));
    }

    public static String key(String evalName, EvalInput input) {
        String context = input.context().stream()
            .map(doc -> doc.getText() == null ? "" : doc.getText())
            .collect(Collectors.joining("|"));
        return String.join("||", evalName, input.query(), input.output(), context);
    }

    private static JsonMapper objectMapper() {
        return JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
    }

    private static List<String> contextTexts(EvalInput input) {
        return input.context().stream()
            .map(doc -> doc.getText() == null ? "" : doc.getText())
            .toList();
    }
}