package example.foodscanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoodScannerConfig {

    @Bean
    public ChatClient.Builder judge(ChatModel model) {
        return ChatClient.builder(model);
    }
}