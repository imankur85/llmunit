package io.llmunit.model;

import org.springframework.ai.chat.model.ChatModel;

public interface LLMProvider {

    String generate(String prompt);

    default String name() {
        return getClass().getSimpleName();
    }

    default ChatModel chatModel() {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " does not expose a ChatModel. "
                + "Register one via ChatClientProvider.registerChatModel(ChatModel).");
    }
}