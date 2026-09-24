package io.llmunit.model.provider;

import io.llmunit.mock.MockResponseStore;
import io.llmunit.model.LLMProvider;
import org.springframework.ai.chat.model.ChatModel;

/**
 * {@code LLMProvider} that serves responses from a {@code MockResponseStore}, falling back to
 * a live delegate provider and recording its responses for later replay.
 */
public class MockProvider implements LLMProvider {

    private final LLMProvider delegate;
    private final MockResponseStore store;

    public MockProvider(LLMProvider delegate) {
        this(delegate, MockResponseStore.instance());
    }

    public MockProvider(LLMProvider delegate, MockResponseStore store) {
        this.delegate = delegate;
        this.store = store;
    }

    @Override
    public String generate(String prompt) {
        return store.lookup(prompt).orElseGet(() -> {
            if (delegate == null) {
                return "";
            }
            String response = delegate.generate(prompt);
            store.record(prompt, response);
            return response;
        });
    }

    @Override
    public ChatModel chatModel() {
        return delegate == null ? null : delegate.chatModel();
    }
}