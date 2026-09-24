package io.llmunit.model;

import io.llmunit.model.provider.MockProvider;
import io.llmunit.model.provider.OllamaProvider;
import java.util.function.Supplier;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Provides the {@code ChatClient.Builder} used by evaluators and manages the active
 * {@code LLMProvider} plus the global offline (mock) flag.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/client/ChatClient.html">ChatClient</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/model/ChatModel.html">ChatModel</a>
 */
public final class ChatClientProvider {

    private static volatile Supplier<LLMProvider> providerSupplier = OllamaProvider::new;
    private static volatile ChatModel registeredChatModel;
    private static volatile boolean offline;

    private ChatClientProvider() {
    }

    public static void register(LLMProvider provider) {
        providerSupplier = () -> provider;
    }

    public static void registerChatModel(ChatModel chatModel) {
        registeredChatModel = chatModel;
    }

    public static void setOffline(boolean value) {
        offline = value;
    }

    public static boolean isOffline() {
        return offline;
    }

    public static LLMProvider provider() {
        if (offline) {
            return new MockProvider(null);
        }
        return providerSupplier.get();
    }

    public static ChatClient.Builder chatClientBuilder() {
        if (registeredChatModel != null) {
            return ChatClient.builder(registeredChatModel);
        }
        return ChatClient.builder(provider().chatModel());
    }
}