package io.llmunit.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Supplies an {@link LLMJudge} bean backed by the application's {@link ChatClient.Builder}. The
 * {@value LLMJudge#MODEL_PROPERTY} property optionally switches the judge to a different model.
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/4.1.0/reference/html/features.html#features.developing-auto-configuration">Spring Boot auto-configuration</a>
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration")
@ConditionalOnClass(ChatClient.Builder.class)
@ConditionalOnBean(ChatClient.Builder.class)
public class LLMUnitJudgeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMJudge llmJudge(ObjectProvider<ChatClient.Builder> builders, Environment environment) {
        ChatClient.Builder appBuilder = builders.getIfUnique();
        if (appBuilder == null) {
            throw new IllegalStateException(
                "llmunit: found multiple ChatClient.Builder beans; define exactly one or a @Primary builder");
        }
        return LLMJudge.using(appBuilder, environment.getProperty(LLMJudge.MODEL_PROPERTY));
    }
}
