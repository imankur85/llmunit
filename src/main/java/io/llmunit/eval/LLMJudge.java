package io.llmunit.eval;

import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * The evaluator ("judge") model used by llmunit evals.
 *
 * <p>The judge reuses the application's {@link ChatClient.Builder}, so it inherits the application's
 * provider, credentials, and defaults. Set {@value #MODEL_PROPERTY} to run the judge on a different
 * model; when the property is absent or blank the application's model is used unchanged.
 *
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/client/ChatClient.Builder.html">ChatClient.Builder</a>
 * @see <a href="https://docs.spring.io/spring-ai/docs/2.0.0/api/org/springframework/ai/chat/prompt/ChatOptions.html">ChatOptions</a>
 */
public final class LLMJudge {

    public static final String MODEL_PROPERTY = "llmunit.judge.model";

    private final ChatClient.Builder builder;

    private LLMJudge(ChatClient.Builder builder) {
        this.builder = builder;
    }

    /** Wraps the application's builder without changing the model. */
    public static LLMJudge using(ChatClient.Builder appBuilder) {
        return new LLMJudge(Objects.requireNonNull(appBuilder, "appBuilder"));
    }

    /** Wraps the application's builder, overriding the model when {@code model} is not blank. */
    public static LLMJudge using(ChatClient.Builder appBuilder, String model) {
        Objects.requireNonNull(appBuilder, "appBuilder");
        if (model == null || model.isBlank()) {
            return new LLMJudge(appBuilder);
        }
        ChatClient.Builder judge = appBuilder.clone()
            .defaultOptions(ChatOptions.builder().model(model.trim()));
        return new LLMJudge(judge);
    }

    public ChatClient.Builder builder() {
        return builder;
    }
}
