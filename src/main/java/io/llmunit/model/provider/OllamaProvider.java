package io.llmunit.model.provider;

import io.llmunit.model.LLMProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;

public class OllamaProvider implements LLMProvider {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";

    private final ChatModel chatModel;

    public OllamaProvider() {
        this(DEFAULT_BASE_URL, OllamaModel.LLAMA3_1);
    }

    public OllamaProvider(String baseUrl, OllamaModel model) {
        this.chatModel = OllamaChatModel.builder()
            .ollamaApi(OllamaApi.builder().baseUrl(baseUrl).build())
            .options(OllamaChatOptions.builder().model(model).build())
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