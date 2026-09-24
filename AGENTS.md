# AGENTS.md

Guidance for AI agents working in this repository.

## Project

`llmunit` is a Java library for unit testing LLMs. It is a Java port of deepeval, built on JUnit Jupiter and Spring AI. The full spec lives in `PRODUCT.md`; docs for the current code are in `README.md` and `USAGE.md`.

- Java 21, Maven build (`pom.xml`), Spring AI BOM 2.0.0, JUnit Jupiter API 6.1.1.
- Package root: `io.llmunit`.

## Mental model

`llmunit` evaluates output the user's code produces. The user calls their own system to get a `String`, then asserts it via:

```java
assertThatLLM(output)
    .withQuery(query)
    .withContext(documents)
    .passesEval(new RelevanceEval(judge))
    .passesEval(new ToxicityEval(judge));
```

An `Eval` maps `EvalInput(query, output, context)` → `EvalResult(passed, score, feedback)`. That is the whole contract.

## Build commands

```bash
mvn test        # compile + run tests
mvn -q test     # quiet (preferred)
mvn clean install
```

- The Maven wrapper (`./mvnw`) is broken. Always use `mvn` (installed via Homebrew).
- Tests are JUnit Jupiter. They must pass without a live model: `JudgeEvalTest` uses a stub
  `ChatModel` (`src/test/java/io/llmunit/support/StubChatModel`), and the offline tests
  (`LLMAssertOfflineTest`, `OfflineFileReplayTest`, `RecordGoldenTest`, `AppTest`) seed or
  replay recorded results.

## Conventions

- Public names use the `LLM` prefix, never the old `LLMUnit` or `Metric` names:
  `@LLMTest`, `LLMTestExtension`, `LLMTestFilter`, `assertThatLLM`, `Eval`/`EvalInput`/`EvalResult`.
- `@LLMTest` is a meta-annotation: `@TestTemplate` + `@ExtendWith(LLMTestExtension.class)` with
  `trials` (int, default 1) and `passRate` (double, default 1.0).
- `@TestTemplate` was chosen because JUnit 6 forbids re-invoking `InvocationInterceptor.invocation.proceed()`
  more than once. Trials are separate template invocations; the extension aggregates pass/fail per trial
  in `TrialStats` (class-scoped store) and fails from the last invocation's `afterEach`.
- The extension holds no static mutable state: the `EvalResultStore` is per test class (extension
  store), and the current `TrialRecorder` is a `ThreadLocal` cleared per invocation.
- `LLMTestExtension.recorder()` returns the current trial's `TrialRecorder` (store + offline flag +
  collected results) inside an `@LLMTest` body; `LLMAssert` records each eval result there.
- `passesEval` never throws inside a trial (results are recorded); pass/fail decisions happen in the
  extension's aggregation.
- Offline mode (`offline=true`): replay `EvalResult`s keyed by `EvalResultStore.key(name, EvalInput)`;
  a lookup miss produces a failing result with a "no recorded result" message. Records can be
  persisted/replayed as JSON golden files. System properties: `llmunit.offline` (force replay),
  `llmunit.recordGolden` (save run results as pretty JSON), `llmunit.recordsDir` (default
  `src/test/resources/llmunit-records`, file per class `<ClassName>.json`). `EvalResultStore` is
  backed by Jackson 3 (`tools.jackson`, via Jackson 3.1.4 pulled in by spring-ai); records are Java
  `record`s (`RecordedEvaluation`), so the Maven compiler must set `<release>21</release>`.
- Guardrail evals (`AbstractJudgeEval`: toxicity, prompt-injection, bias) ask the judge for a violation
  probability in [0,1] and report `1 - violation` as the quality score, so higher is always better.
- `assert` is a Java keyword — the assertion package is `io.llmunit.assertion` (NOT `assert`).

## Key API facts (Spring AI 2.0.0)

- Maven must declare the jupiter-api transitives explicitly:
  `opentest4j` and `junit-platform-commons` (both `provided`), plus `junit-jupiter-engine` (`test`).
- `maven-compiler-plugin` must set `<parameters>true</parameters>` so parameter names are available at runtime.
- `ChatClient` has no `.content()` in 2.0 — extract text via
  `...call().chatClientResponse().chatResponse().getResult().getOutput().getText()`.
- Build `ChatClient.Builder` with `ChatClient.builder(ChatModel)`.
- `Document(String)` lives in `spring-ai-commons` (`org.springframework.ai.document.Document`).
- Spring AI evals: `org.springframework.ai.chat.evaluation.{RelevancyEvaluator, FactCheckingEvaluator}`;
  `RelevancyEvaluator(ChatClient.Builder)`, `FactCheckingEvaluator.forBespokeMinicheck(ChatClient.Builder)`.
- `EvaluationRequest(String, List<Document>, String)`, `EvaluationResponse(boolean, float, String, Map)`.
- In 2.0 there is no `AiMessage`; assistant output is built with `AssistantMessage.builder().content(...)`
  wrapped in `Generation`, then `ChatResponse(List<Generation>)` (see `StubChatModel`).
- A stub `ChatModel` only needs to implement `call(Prompt)`; every other method has a default in 2.0.

## Source layout

- `annotations/` — `@LLMTest` (`trials`, `passRate`, `offline`).
- `assertion/` — `LLMAssert`, the fluent `assertThatLLM(...)` chain.
- `eval/` — `Eval`, `EvalInput`, `EvalResult`, `AbstractEval`, `AbstractJudgeEval`, `SpringAIEval`,
  and the built-in evals: `RelevanceEval`, `FactCheckingEval`, `ToxicityEval`, `PromptInjectionEval`, `BiasEval`.
- `extension/` — `LLMTestExtension` (test template provider + trial aggregation), `LLMTestFilter`.
- `mock/` — `EvalResultStore` + `RecordedEvaluation` (per-class record/replay store, Jackson 3 JSON).
- `examples/food-scanner/` — standalone Spring Boot 4.1 project demonstrating `@SpringBootTest`
  usage; NOT part of the reactor. Build it with `mvn -q install` at the root first, then
  `mvn test` inside `examples/food-scanner`.

## Rules

- Never introduce comments unless asked.
- Match existing code style (small classes, accessor-style methods like `query()`/`withContext(...)`).
- No secrets or API keys in code or docs.