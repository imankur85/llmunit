# llmunit

`llmunit` is a Java library for unit testing large language models (LLMs). It is the Java port of [deepeval](https://deepeval.com/), built on JUnit Jupiter and Spring AI.

`llmunit` evaluates the output **your code** produces. The flow is:

1. Call your system (a service, agent, or RAG chain) to get a `String` output.
2. Describe the evaluation input — the original query and any grounding context (documents your RAG step retrieved, conversation history, etc.).
3. Assert that the output passes one or more evaluators.

An [evaluator](https://github.com/imankur85/llmunit/blob/main/USAGE.md#evaluators) maps an `EvalInput` (query, output, context) to an `EvalResult` (passed, score, feedback). That is the whole contract.

## Features

- `@LLMTest` annotation with non-determinism handling: `trials` re-executes the test body, `passRate` tolerates flaky model output.
- Fluent assertions: `assertThatLLM(output).withQuery(...).withContext(...).passesEval(...)`.
- Built-in evaluators, all taking a single `ChatClient.Builder`:
  - Quality (wraps Spring's stock evaluators): `RelevanceEval`, `FactCheckingEval`.
  - Guardrails (our metrics on Spring AI's `Evaluator` + `PromptTemplate` extension point):
    `ToxicityEval`, `PromptInjectionEval`, `BiasEval` — each reports `1 - violation` so higher is better.
- Core is decoupled from Spring: `io.llmunit.core` (eval contract, record/replay store, assertions)
  has no Spring dependencies; `io.llmunit.eval` is the single Spring AI bridge hosting every eval.
- Offline (record/replay) mode — llmunit's own layer, shown in detail in
  [USAGE.md#offline-recordreplay-mode](USAGE.md#offline-recordreplay-mode): evaluation results
  are captured once with a live judge into JSON golden files (`src/test/resources/llmunit-records/`),
  then replayed deterministically on later runs so CI needs no model and stays deterministic.
- No global mutable state: results are stored per test class, not in static singletons.

## Requirements

- Java 21+
- Maven 3.8+
- Spring AI 2.0.0, JUnit Jupiter API 6.1.1

## Getting started

See [USAGE.md](USAGE.md) for detailed usage, examples, and configuration, and
[`examples/food-scanner`](examples/food-scanner) for a complete `@SpringBootTest` project that
tests a real Spring service with these assertions.

Quick example:

```java
@LLMTest(trials = 5, passRate = 0.8)
void testProductSuggestion() {
    String answer = foodScanner.suggest("coca cola");
    assertThatLLM(answer)
        .withQuery("a cleaner alternative to Coca Cola")
        .withContext(retrievedProductDocs)
        .passesEval(new FactCheckingEval(builder))
        .passesEval(new RelevanceEval(builder));
}
```

## Build

```bash
mvn clean install
```

## License

TBD