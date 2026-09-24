package io.llmunit.model.provider;

import com.openai.client.OpenAIClient;
import io.llmunit.model.LLMProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * {@code LLMProvider} backed by an OpenAI client through Spring AI's {@code OpenAiChatModel}.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/openai/OpenAiChatModel.html">OpenAiChatModel</a>
 */
public class OpenAiProvider implements LLMProvider {

    private final ChatModel chatModel;

    public OpenAiProvider(OpenAIClient client) {
        this(client, "gpt-4o");
    }

    public OpenAiProvider(OpenAIClient client, String model) {
        this.chatModel = OpenAiChatModel.builder()
            .openAiClient(client)
            .options(OpenAiChatOptions.builder().model(model).build())
            .build();
    }

    @Override
    public String generate(String prompt) {
        return chatModel.call(prompt);
    }

    @Override
    public ChatModel chatModel() {
        return chatModel;
    }
}