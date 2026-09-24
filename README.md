# llmunit

`llmunit` is a Java library for unit testing large language models (LLMs), following the same ideas as JUnit. It is the Java port of [deepeval](https://deepeval.com/).

The library is built on top of Spring AI's test framework and the JUnit Jupiter test runner:

- https://docs.spring.io/spring-ai/reference/api/testing.html
- https://deepeval.com/docs/metrics-introduction

## Features

- `@LLMTest` annotation, a meta-annotation that wraps the JUnit `@Test` annotation.
- Assertion annotations for unit testing LLM behavior:
  - `@AssertFunctionality` - verifies the model fulfills the expected functionality.
  - `@AssertToxicity` - guardrail eval that fails on toxic responses.
  - `@AssertRelevance` - checks that responses are relevant to the query/context.
  - `@AssertPromptInjection` - guardrail eval that detects prompt-injection compliance.
- `@Metric` annotation to attach named metrics (e.g. `FAITHFULNESS`) with a threshold.
- Offline (mock) mode that records and replays LLM responses, so tests run without a model.
- LLM-provider abstraction, so tests do not depend on a specific provider.
- Built-in evals powered by LLM-as-a-judge, fact-checking, and guardrail evaluations.

## Requirements

- Java 21+
- Maven 3.8+

## Getting started

See [USAGE.md](USAGE.md) for detailed usage, examples, and configuration.

Quick example:

```java
@LLMTest(
    prompt = "You are a helpful assistant. Answer the following question: {{question}}",
    expected = "You are a helpful assistant. Answer the following question: {{question}}",
    offline = true
)
void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

## Build

```bash
mvn clean install
```

## License

TBD