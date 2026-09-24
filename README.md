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
- Built-in evaluators:
  - Quality: `RelevanceEval`, `FactCheckingEval`.
  - Guardrails: `ToxicityEval`, `PromptInjectionEval`, `BiasEval`.
- Offline (record/replay) mode: recorded evaluation results are replayed, so tests run deterministically without a judge model.
- No provider abstraction — create evals directly from a Spring AI autowired `ChatClient.Builder`.
- No global mutable state: results are stored per test class, not in static singletons.

## Requirements

- Java 21+
- Maven 3.8+
- Spring AI 2.0.0, JUnit Jupiter API 6.1.1

## Getting started

See [USAGE.md](USAGE.md) for detailed usage, examples, and configuration.

Quick example:

```java
@LLMTest(trials = 5, passRate = 0.8)
void testProductSuggestion() {
    String answer = foodScanner.suggest("coca cola");
    assertThatLLM(answer)
        .withQuery("a cleaner alternative to Coca Cola")
        .withContext(retrievedProductDocs)
        .passesEval(new FactCheckingEval(judge))
        .passesEval(new RelevanceEval(judge));
}
```

## Build

```bash
mvn clean install
```

## License

TBD