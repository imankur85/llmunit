package io.llmunit.mock;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import io.llmunit.eval.EvalInput;
import io.llmunit.eval.EvalResult;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Per-test-class store of recorded evaluation results, keyed by eval name and input.
 * Replayed verbatim in offline mode so tests run without contacting a judge model.
 */
public class EvalResultStore {

    private final Map<String, EvalResult> results = new ConcurrentHashMap<>();

    public EvalResultStore record(String key, EvalResult result) {
        results.put(key, result);
        return this;
    }

    public Optional<EvalResult> lookup(String key) {
        return Optional.ofNullable(results.get(key));
    }

    public void clear() {
        results.clear();
    }

    public static String key(String evalName, EvalInput input) {
        String context = input.context().stream()
            .map(doc -> doc.getText() == null ? "" : doc.getText())
            .collect(Collectors.joining("|"));
        return String.join("||", evalName, input.query(), input.output(), context);
    }

    public boolean has(String key) {
        return results.containsKey(key);
    }

    public int size() {
        return results.size();
    }

    public Map<String, EvalResult> snapshot() {
        return Map.copyOf(results);
    }
}