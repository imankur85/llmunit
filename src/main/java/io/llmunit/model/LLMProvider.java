package io.llmunit.model;

import org.springframework.ai.chat.model.ChatModel;

/**
 * Abstraction over an LLM backend: text generation for a prompt plus an optional
 * {@code ChatModel} for evaluation, independent of the concrete provider.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/model/ChatModel.html">ChatModel</a>
 */
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