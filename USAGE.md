# llmunit Usage

## Prerequisites

`llmunit` requires Java 21+ and Maven. Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>io.llmunit</groupId>
    <artifactId>llmunit</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

The library depends on Spring AI's testing support and the JUnit Jupiter runner, which are pulled in transitively.

## Writing a test

Use `@LLMTest` as a drop-in replacement for `@Test` on methods that exercise an LLM:

```java
import io.llmunit.annotations.LLMTest;
import org.junit.jupiter.api.Test;

@Test
@LLMTest(
    prompt = "You are a helpful assistant. Answer the following question: {{question}}",
    expected = "You are a helpful assistant. Answer the following question: {{question}}"
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

Test method parameters are resolved automatically from the `{{placeholder}}` entries in the prompt template.

### Registering test inputs

Values for the placeholders are registered before the test run:

```java
LLMTestExtension.registerInput("question", "What is 2+2?");
```

## Assertions

A test method can declare one or more assertion annotations. Each assertion evaluates the LLM
response and fails the test if the score is below the configured threshold.

### Functionality

```java
@AssertFunctionality(
    expected = "You are a helpful assistant. Answer the following question: {{question}}"
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

### Toxicity

```java
@AssertToxicity(threshold = 0.7)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

### Relevance

```java
@AssertRelevance(threshold = 0.7)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

### Prompt injection

```java
@AssertPromptInjection(threshold = 0.7)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

## Metrics

Named metrics can be attached with the repeatable `@Metric` annotation:

```java
@Metric(
    metric = MetricEnum.FAITHFULNESS,
    threshold = 0.7,
    key = "functionality",
    type = "string"
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}
```

Supported `MetricEnum` values: `FAITHFULNESS`, `ANSWER_RELEVANCY`, `RELEVANCY`,
`HALLUCINATION`, `TOXICITY`, `PROMPT_INJECTION`, `FUNCTIONALITY`, `BIAS`,
`CONTEXTUAL_RELEVANCY`.

## Offline (mock) mode

Set `offline = true` on `@LLMTest` to run tests without contacting a live model. Responses are
recorded into and replayed from an in-memory `MockResponseStore`:

```java
@LLMTest(prompt = "Answer: {{question}}", expected = "Answer: {{question}}", offline = true)
void offlineTest(String question) {
    // runs without any LLM call
}
```

Pre-record responses for fully deterministic offline tests:

```java
MockResponseStore.instance().record("Answer: What is 2+2?", "4");
```

## Provider configuration

By default `llmunit` uses a local Ollama provider (`llama3.1:8b` at `http://localhost:11434`).

Register your own provider or a Spring AI `ChatModel` directly:

```java
// Custom LLMProvider (e.g. OpenAI)
ChatClientProvider.register(new OpenAiProvider(openAIClient));

// Or any Spring AI ChatModel
ChatClientProvider.registerChatModel(myChatModel);
```