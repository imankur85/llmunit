# llmunit Usage

## Mental model

`llmunit` evaluates output your code produces:

1. Call your system (a service, agent, or chain) to get a `String` output.
2. Describe the evaluation input — the original query and any grounding context (documents your RAG step retrieved, previous conversation history, etc.).
3. Assert that the output passes one or more evaluators.

An evaluator maps an `EvalInput` (query, output, context) to an `EvalResult` (passed, score, feedback). That is the whole contract.

## Prerequisites

`llmunit` requires Java 21+, Maven, and Spring AI 2.0.0.

Provide a `ChatClient.Builder` bean in your Spring configuration. The judge evals are built from it:

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
    ChatClient.Builder judge;

    @LLMTest(trials = 5, passRate = 0.8)
    void testProductSuggestion() {
        String answer = scanner.suggest("coca cola");
        assertThatLLM(answer)
            .withQuery("a cleaner alternative to Coca Cola")
            .withContext(retrievedProductDocs)
            .passesEval(new FactCheckingEval(judge))
            .passesEval(new RelevanceEval(judge));
    }
}
```

- `@LLMTest` is a JUnit test template: the body is executed `trials` times and the test fails only if fewer than `passRate` of the trials passed. This handles non-deterministic model output.
- The test body calls your own system — the framework never generates the output for you.
- `assertThatLLM(...)` records each eval result; the extension aggregates them per trial.

## The assertion chain

`assertThatLLM(output)` returns a `LLMAssert`:

- `withQuery(String)` — the original user query.
- `withContext(List<Document> | Document... | String...)` — grounding context (e.g. retrieved RAG docs).
- `passesEval(Eval)` — asserts the output passes the eval, using the eval's threshold.
- `passesEval(Eval, double threshold)` — asserts with an explicit threshold.

The chain is fluent and can combine any number of evals.

## Evaluators

### Quality

- `RelevanceEval(ChatClient.Builder)` — how relevant the response is to the query/context.
- `FactCheckingEval(ChatClient.Builder)` — whether the response stays faithful to the grounding context (covers faithfulness and hallucination).

### Guardrails

- `ToxicityEval(ChatClient.Builder)` — fails on offensive, harmful, hateful, or unsafe content.
- `PromptInjectionEval(ChatClient.Builder)` — fails when the model followed an injected instruction.
- `BiasEval(ChatClient.Builder)` — fails on harmful stereotypes/bias in the response.

Guardrail evals report a **quality score**: the judge returns the probability of a violation and the eval reports `1 - violation`, so a higher score always means a safer response. The default threshold is `0.5`; each eval has a `(builder, threshold)` constructor overload.

### Custom evaluators

Implement `Eval` — one method maps an `EvalInput` to an `EvalResult`:

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

With `offline = true`, judge evaluations are replayed from a per-test-class `EvalResultStore` instead of calling a live model — deterministic, no model required.

Record results in one test (online), replay them in another (offline) within the same test class:

```java
@LLMTest
void recordGoldenResults() {
    // online run: evals are executed and results recorded automatically
    assertThatLLM(service.run("q"))
        .withQuery("q")
        .passesEval(new RelevanceEval(judge));
}

@LLMTest(offline = true)
void replayRecordedResults() {
    assertThatLLM("canned output")
        .withQuery("q")
        .passesEval(new RelevanceEval(judge)); // replayed from the store
}
```

You can also seed the store manually from inside a test body:

```java
EvalInput input = new EvalInput("q", "a", List.of());
LLMTestExtension.recorder().store().record(
    EvalResultStore.key("relevance", input),
    new EvalResult("relevance", 0.9, 0.5, "ok"));
```

If an offline evaluation has no recorded result, it fails with a clear message.

## Non-determinism handling

- `trials` (default `1`) — number of times the test body is executed.
- `passRate` (default `1.0`) — the fraction of trials that must pass (threshold `passRate * trials`, rounded up).

Example: `@LLMTest(trials = 5, passRate = 0.8)` requires at least 4 of 5 trials to pass. Use this when your system or your judge is inherently non-deterministic.