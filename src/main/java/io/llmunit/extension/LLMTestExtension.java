package io.llmunit.extension;

import io.llmunit.annotations.LLMTest;
import io.llmunit.context.EvaluationResult;
import io.llmunit.context.LLMTestContext;
import io.llmunit.model.ChatClientProvider;
import io.llmunit.mock.MockResponseStore;
import io.llmunit.util.PromptTemplateRenderer;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.opentest4j.AssertionFailedError;

/**
 * JUnit Jupiter extension backing {@code @LLMTest}: registers inputs, resolves placeholder
 * parameters, generates the actual output, and runs the evaluation assertions after each test.
 *
 * @see <a href="https://docs.spring.io/spring-ai/reference/api/testing.html">Spring AI testing</a>
 */
public class LLMTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(LLMTestExtension.class);
    private static final String CONTEXT_KEY = "llmunit.context";

    private static final Map<String, Object> REGISTERED_INPUTS = new ConcurrentHashMap<>();

    public static void registerInput(String parameterName, Object value) {
        REGISTERED_INPUTS.put(parameterName, value);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        if (!LLMTestFilter.isLLMTest(testMethod)) {
            return;
        }
        LLMTest annotation = LLMTestFilter.annotation(testMethod);
        ChatClientProvider.setOffline(annotation.offline());
        context.getStore(NAMESPACE).put(CONTEXT_KEY, new LLMTestContext(testMethod, annotation));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (!LLMTestFilter.isLLMTest(testMethod)) {
            return false;
        }
        Class<?> type = parameterContext.getParameter().getType();
        if (LLMTestContext.class.isAssignableFrom(type)) {
            return true;
        }
        String name = parameterContext.getParameter().getName();
        Set<String> placeholders =
            PromptTemplateRenderer.extractPlaceholders(LLMTestFilter.annotation(testMethod).prompt());
        return name != null && placeholders.contains(name);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        LLMTestContext context = getContext(extensionContext);
        if (context == null) {
            throw new ParameterResolutionException(
                "LLMTestContext was not initialized for: " + extensionContext.getRequiredTestMethod());
        }
        Class<?> type = parameterContext.getParameter().getType();
        if (LLMTestContext.class.isAssignableFrom(type)) {
            return context;
        }
        if (type != String.class) {
            throw new ParameterResolutionException("Unsupported parameter type " + type.getName()
                + " for @LLMTest test method. Supported types: String and LLMTestContext.");
        }
        String name = parameterContext.getParameter().getName();
        Object value = REGISTERED_INPUTS.getOrDefault(name, "");
        context.parameters().put(name, value);
        return value;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        LLMTestContext context = getContext(extensionContext);
        if (context == null) {
            return;
        }

        String prompt = PromptTemplateRenderer.render(context.annotation().prompt(), context.parameters());
        context.setInput(prompt);

        String expected = PromptTemplateRenderer.render(context.annotation().expected(), context.parameters());
        context.setExpectedOutput(expected);

        if (context.actualOutput() == null) {
            if (ChatClientProvider.isOffline()) {
                context.setActualOutput(MockResponseStore.instance().lookup(prompt).orElse(""));
            } else {
                context.setActualOutput(ChatClientProvider.provider().generate(prompt));
            }
        }

        if (ChatClientProvider.isOffline()) {
            return;
        }

        LLMAssertionEvaluator evaluator = new LLMAssertionEvaluator(ChatClientProvider.chatClientBuilder());
        List<EvaluationResult> results = evaluator.evaluate(context);
        context.addResults(results);

        for (EvaluationResult result : results) {
            if (!result.passed()) {
                throw new AssertionFailedError("Metric '" + result.metricName() + "' failed: score="
                    + result.score() + " threshold=" + result.threshold()
                    + " feedback=" + result.feedback());
            }
        }
    }

    private LLMTestContext getContext(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).get(CONTEXT_KEY, LLMTestContext.class);
    }
}