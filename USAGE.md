# llmunit Usage

## Mental model

`llmunit` evaluates output your code produces:

1. Call your system (a service, agent, or chain) to get a `String` output.
2. Describe the evaluation input — the original query and any grounding context (documents your RAG step retrieved, previous conversation history, etc.).
3. Assert that the output passes one or more evaluators.

An evaluator maps an `EvalInput` (query, output, context) to an `EvalResult` (passed, score, feedback). That is the whole contract.

## Prerequisites

`llmunit` requires Java 21+, Maven, and (for the Spring AI evals) Spring AI 2.0.0.

The library is split into a Spring-free core (`io.llmunit.core`) and a Spring AI bridge
(`io.llmunit.eval`). The core — `Eval`, `EvalInput`, `EvalResult`, `LLMAssert`, and the
record/replay store — has no Spring dependencies. All evaluators live in `io.llmunit.eval` and
run on top of Spring AI's evaluation machinery: the quality evals (`RelevanceEval`,
`FactCheckingEval`) wrap Spring's stock evaluators, and the guardrails (`ToxicityEval`,
`PromptInjectionEval`, `BiasEval`) are our metrics built on Spring's `Evaluator` + `PromptTemplate`
extension point — a higher score always means a safer response. Every eval takes a single
`ChatClient.Builder`.

Provide a `ChatClient.Builder` bean in your Spring configuration:

```java
@Bean
ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
    return ChatClient.builder(chatModel);
}
```

## Writing a test

```java
@SpringBootTest
@ExtendWith(LLMTestExtension.class)
class ProductSuggestionTest {

    @Autowired
    FoodScanner scanner;

    @Autowired
    ChatClient.Builder builder;

    @LLMTest(trials = 5, passRate = 0.8)
    void testProductSuggestion() {
        String answer = scanner.suggest("coca cola");
        assertThatLLM(answer)
            .withQuery("a cleaner alternative to Coca Cola")
            .withContext(retrievedProductDocs)
            .passesEval(new FactCheckingEval(builder))
            .passesEval(new RelevanceEval(builder))
            .passesEval(new ToxicityEval(builder));
    }
}
```

- `@LLMTest` is a JUnit test template: the body is executed `trials` times and the test fails only if fewer than `passRate` of the trials passed. This handles non-deterministic model output.
- The test body calls your own system — the framework never generates the output for you.
- `assertThatLLM(...)` records each eval result; the extension aggregates them per trial.

## The assertion chain

`assertThatLLM(output)` returns a `LLMAssert`:

- `withQuery(String)` — the original user query.
- `withContext(List<String> | String...)` — grounding context (e.g. retrieved RAG docs as plain text).
- `passesEval(Eval)` — asserts the output passes the eval, using the eval's threshold.
- `passesEval(Eval, double threshold)` — asserts with an explicit threshold.

The chain is fluent and can combine any number of evals.

## Evaluators

### Quality

- `RelevanceEval(ChatClient.Builder)` — how relevant the response is to the query/context.
- `FactCheckingEval(ChatClient.Builder)` — whether the response stays faithful to the grounding context (covers faithfulness and hallucination).

Both wrap Spring AI's stock evaluators (see `RelevancyEvaluator`, `FactCheckingEvaluator`).

### Guardrails

- `ToxicityEval(ChatClient.Builder)` — fails on offensive, harmful, hateful, or unsafe content.
- `PromptInjectionEval(ChatClient.Builder)` — fails when the model followed an injected instruction.
- `BiasEval(ChatClient.Builder)` — fails on harmful stereotypes/bias in the response.

Guardrail evals are our metrics implemented on Spring AI's `Evaluator` + `PromptTemplate` extension
point: the model returns the probability of a violation and the metric reports `1 - violation` as a
**quality score**, so a higher score always means a safer response. The default threshold is `0.5`;
each eval has a `(builder, threshold)` constructor overload.

### Custom evaluators

Implement `io.llmunit.core.Eval` — one method maps an `EvalInput` to an `EvalResult`:

```java
class CustomEval implements Eval {
    @Override public String name() { return "custom"; }
    @Override public double threshold() { return 0.7; }
    @Override public EvalResult evaluate(EvalInput input) {
        double score = scoreOutput(input);
        return new EvalResult(name(), score, threshold(), "details");
    }
}
```

## Offline (record/replay) mode

With `offline = true`, evaluations are replayed from recorded `EvalResult`s instead of calling a judge model — deterministic, no model required.

Records live in a per-test-class `EvalResultStore`, keyed by eval name + query + output + context. The store can be seeded from a test body (same JVM) or loaded from a JSON golden file (across runs).

### In-session seeding

```java
@LLMTest(offline = true)
void replaysSeededResult() {
    EvalInput input = new EvalInput("q", "a", List.of());
    LLMTestExtension.recorder().store().record(
        "relevance", input, new EvalResult("relevance", 0.9, 0.5, "ok"));

    assertThatLLM("a")
        .withQuery("q")
        .passesEval(new RelevanceEval(builder)); // replays the seeded result
}
```

*Note:* `EvalInput`, `EvalResult`, `EvalResultStore`, and `LLMAssert` live in `io.llmunit.core`.

If an offline evaluation has no recorded result, it fails with a clear message.

### Golden files (across runs)

A recording run executes the evals with a live judge and persists the results as pretty JSON:

```bash
mvn test -Dllmunit.recordGolden=true     # recording run (needs a model)
```

Each test class writes `src/test/resources/llmunit-records/<ClassName>.json` (one JSON per eval). Review and commit the golden file. Later runs replay it without a model:

```java
@LLMTest(offline = true)
void replaysGoldenFile() {
    assertThatLLM("a").withQuery("q").passesEval(new RelevanceEval(builder));
}
```

Offline tests load `<ClassName>.json` when it exists and fail with "no recorded result" for anything not in it — so a new query/context/version of the output surfaces as a test failure.

### System properties

| Property | Default | Effect |
| --- | --- | --- |
| `llmunit.offline` | annotation value | Forces every `@LLMTest` to replay (CI-friendly). |
| `llmunit.recordGolden` | `false` | Saves the run's results to each class's JSON golden file. |
| `llmunit.recordsDir` | `src/test/resources/llmunit-records` | Where golden files are read from and written to. |

## Non-determinism handling

- `trials` (default `1`) — number of times the test body is executed.
- `passRate` (default `1.0`) — the fraction of trials that must pass (threshold `passRate * trials`, rounded up).

Example: `@LLMTest(trials = 5, passRate = 0.8)` requires at least 4 of 5 trials to pass. Use this when your system or your judge is inherently non-deterministic.