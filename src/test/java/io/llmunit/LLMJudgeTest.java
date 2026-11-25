package io.llmunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.llmunit.eval.LLMJudge;
import io.llmunit.eval.LLMUnitJudgeAutoConfiguration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class LLMJudgeTest {

    @Test
    void usesApplicationModelWhenNoOverrideIsConfigured() {
        CapturingChatModel model = new CapturingChatModel();
        ChatClient client = LLMJudge.using(ChatClient.builder(model)).builder().build();

        client.prompt().user("q").call().content();

        assertNull(model.seenModel);
    }

    @Test
    void overridesModelWhenConfigured() {
        CapturingChatModel model = new CapturingChatModel();
        ChatClient client = LLMJudge.using(ChatClient.builder(model), "judge-model").builder().build();

        client.prompt().user("q").call().content();

        assertEquals("judge-model", model.seenModel);
    }

    @Test
    void blankOverrideFallsBackToApplicationModel() {
        CapturingChatModel model = new CapturingChatModel();
        ChatClient client = LLMJudge.using(ChatClient.builder(model), "  ").builder().build();

        client.prompt().user("q").call().content();

        assertNull(model.seenModel);
    }

    @Test
    void autoConfigurationReadsOptionalProperty() {
        CapturingChatModel model = new CapturingChatModel();
        ChatClient.Builder appBuilder = ChatClient.builder(model);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("appBuilder", appBuilder);

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
            new MapPropertySource("test", Map.of(LLMJudge.MODEL_PROPERTY, "judge-from-property")));

        LLMJudge judge = new LLMUnitJudgeAutoConfiguration()
            .llmJudge(beanFactory.getBeanProvider(ChatClient.Builder.class), environment);
        judge.builder().build().prompt().user("q").call().content();

        assertEquals("judge-from-property", model.seenModel);
    }

    @Test
    void autoConfigurationFallsBackToApplicationModel() {
        CapturingChatModel model = new CapturingChatModel();
        ChatClient.Builder appBuilder = ChatClient.builder(model);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("appBuilder", appBuilder);

        LLMJudge judge = new LLMUnitJudgeAutoConfiguration()
            .llmJudge(beanFactory.getBeanProvider(ChatClient.Builder.class), new StandardEnvironment());
        judge.builder().build().prompt().user("q").call().content();

        assertNull(model.seenModel);
    }

    private static final class CapturingChatModel implements ChatModel {

        private String seenModel;

        @Override
        public ChatResponse call(Prompt prompt) {
            seenModel = prompt.getOptions() == null ? null : prompt.getOptions().getModel();
            return new ChatResponse(List.of(
                new Generation(AssistantMessage.builder().content("ok").build())));
        }
    }
}
