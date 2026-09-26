package io.llmunit.support;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Test double for Spring AI {@link ChatModel} that returns a fixed canned response,
 * so evaluators run deterministically without a live model.
 */
public final class StubChatModel implements ChatModel {

    private final String response;

    public StubChatModel(String response) {
        this.response = response == null ? "" : response;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(List.of(
            new Generation(AssistantMessage.builder().content(response).build())));
    }

    public ChatClient.Builder chatClientBuilder() {
        return ChatClient.builder(this);
    }
}