package example.foodscanner;

import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/**
 * In-memory judge model used so the example runs without network access. It answers every
 * guardrail question with a 0.2 violation probability (a safe response), which both example
 * evals accept. Swap in a real provider (see application.properties) to judge real output.
 */
@Component
public class DummyChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(List.of(
            new Generation(AssistantMessage.builder().content("0.2").build())));
    }
}