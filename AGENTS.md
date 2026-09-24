# AGENTS.md

Guidance for AI agents working in this repository.

## Project

`llmunit` is a Java library for unit testing LLMs. It is a Java port of deepeval, built on Spring AI's testing support and JUnit Jupiter. The full spec lives in `PRODUCT.md`; docs for the current code are in `README.md` and `USAGE.md`.

- Java 21, Maven build (`pom.xml`), Spring AI BOM 2.0.0, JUnit Jupiter API 6.1.1.
- Package root: `io.llmunit`.

## Build commands

```bash
mvn test        # compile + run tests
mvn -q test     # quiet (preferred)
mvn clean install
```

- The Maven wrapper (`./mvnw`) is broken. Always use `mvn` (installed via Homebrew).
- Tests are JUnit Jupiter. The current test is an offline smoke test: `mvn -q test` should pass 1/1.

## Conventions

- Naming: public names use the `LLMTest` prefix, never the old `LLMUnit`:
  `@LLMTest` (annotation), `LLMTestExtension`, `LLMTestContext`, `LLMTestFilter`.
  Keeping this consistent matters — do not reintroduce `LLMUnit*` identifiers.
- `@LLMTest` is a meta-annotation: `@Test` + `@ExtendWith(LLMTestExtension.class)`.
- Test method parameters are resolved by name from `{{placeholder}}` entries in the
  annotation `prompt()`, using values registered via `LLMTestExtension.registerInput(...)`.
- `LLMTestFilter.isLLMTest(Method)` gates extension callbacks; non-annotated methods are ignored.
- Offline mode (`offline=true`) skips live generation: `actualOutput` comes from
  `MockResponseStore.instance().lookup(prompt)` and no evaluation assertions run.

## Key API facts (Spring AI 2.0.0)

- Maven must declare the jupiter-api transitives explicitly:
  `opentest4j` and `junit-platform-commons` (both `provided`), plus `junit-jupiter-engine` (`test`).
- `maven-compiler-plugin` must set `<parameters>true</parameters>` so parameter names are
  available at runtime (used to resolve placeholder parameters).
- `ChatClient` has no `.content()` in 2.0 — extract text via
  `...call().chatClientResponse().chatResponse().getResult().getOutput().getText()`.
- Build `ChatClient.Builder` with `ChatClient.builder(ChatModel)` or `ChatClient.builder(builder)`.
- `Document(String)` lives in `spring-ai-commons` (`org.springframework.ai.document.Document`).
- Evaluators: `org.springframework.ai.chat.evaluation.{RelevancyEvaluator, FactCheckingEvaluator}`;
  `RelevancyEvaluator(ChatClient.Builder)`, `FactCheckingEvaluator.forBespokeMinicheck(ChatClient.Builder)`.
- `EvaluationRequest(String, List<Document>, String)`, `EvaluationResponse(boolean, float, String, Map)`.
- Ollama: `OllamaApi.builder().baseUrl(...)`, `OllamaChatOptions.builder().model(OllamaModel.LLAMA3_1)`,
  `OllamaChatModel.builder().ollamaApi(..).options(..)` (no public `OllamaApi(String)` ctor).
- OpenAI: `OpenAiChatModel.builder().openAiClient(OpenAIClient).options(OpenAiChatOptions.builder().model(m).build())`.

## Source layout

- `annotations/` — `@LLMTest`, assertion annotations (`@AssertFunctionality`, `@AssertToxicity`,
  `@AssertRelevance`, `@AssertPromptInjection`), `@Metric`/`@Metrics`, `MetricEnum`.
- `extension/` — `LLMTestExtension` (callbacks + parameter resolution), `LLMTestFilter`, `LLMAssertionEvaluator`.
- `context/` — `LLMTestContext`, `TestParameters`, `EvaluationResult`.
- `metric/` — `LLMMetric` interface, metric implementations, `MetricFactory`.
- `evaluator/` — `EvaluatorBridge`, `LLMJudgeEvaluator`, `SpringAIEvaluatorRegistry`.
- `model/` — `LLMProvider`, `ChatClientProvider`, `provider/` (`OllamaProvider`, `OpenAiProvider`, `MockProvider`).
- `mock/` — `MockResponseStore`, `MockLLMInterceptor`.
- `util/` — `PromptTemplateRenderer`, `ThresholdValidator`. (No third-party utility libraries.)

## Rules

- Never introduce comments unless asked.
- Match existing code style (small classes, accessor-style methods like `input()`/`setInput(...)`).
- No secrets or API keys in code or docs.