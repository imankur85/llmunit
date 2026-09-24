package io.llmunit.mock;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe store of prompt-to-response recordings used for offline replay in tests.
 */
public class MockResponseStore {

    private static final MockResponseStore INSTANCE = new MockResponseStore();

    private final Map<String, String> responses = new ConcurrentHashMap<>();

    public static MockResponseStore instance() {
        return INSTANCE;
    }

    public MockResponseStore record(String prompt, String response) {
        responses.put(normalize(prompt), response);
        return this;
    }

    public Optional<String> lookup(String prompt) {
        return Optional.ofNullable(responses.get(normalize(prompt)));
    }

    public void clear() {
        responses.clear();
    }

    private String normalize(String prompt) {
        return prompt == null ? "" : prompt.trim();
    }
}