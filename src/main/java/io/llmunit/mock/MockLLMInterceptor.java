package io.llmunit.mock;

import io.llmunit.model.LLMProvider;
import java.util.Optional;
import org.springframework.ai.chat.model.ChatModel;

public class MockLLMInterceptor implements LLMProvider {

    private final LLMProvider delegate;
    private final MockResponseStore store;
    private boolean record = true;

    public MockLLMInterceptor(LLMProvider delegate) {
        this(delegate, MockResponseStore.instance());
    }

    public MockLLMInterceptor(LLMProvider delegate, MockResponseStore store) {
        this.delegate = delegate;
        this.store = store;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }

    @Override
    public String generate(String prompt) {
        Optional<String> cached = store.lookup(prompt);
        if (cached.isPresent()) {
            return cached.get();
        }
        String generated = delegate.generate(prompt);
        if (record) {
            store.record(prompt, generated);
        }
        return generated;
    }

    @Override
    public ChatModel chatModel() {
        return delegate.chatModel();
    }
}