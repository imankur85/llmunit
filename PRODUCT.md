llmunit is a library for unit testing large language models similar to JUnit.
It makes use of SprinAI's test framework and Jupiter's test runner.
https://docs.spring.io/spring-ai/reference/api/testing.html

For evals framework refer: 
https://deepeval.com/docs/metrics-introduction


It provides @LLMUnit annotation which is a wrapper around @Test annotation.
It also provides @AssertFunctionality, @AssertToxicity, @AssertRelevance, @AssertPromptInjection annotations for unit testing LLMs.
It can mock the LLM calls to run tests in offline mode as well.
It abstracts away the details of the LLM provider.
It uses various evals like LLM as a judge, fact-checking, similarity evals, guardrail evals using annotations.
It allows you to define metrics for the evals and run them using the library.



A test case in llmunit looks like this: 

@Test
@LLMUnit(
    prompt = "You are a helpful assistant. Answer the following question: {{question}}",
    expected = "You are a helpful assistant. Answer the following question: {{question}}",
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}

A test method can have multiple assertions. An assertion in llmunit looks like this:

@AssertFunctionality(
    expected = "You are a helpful assistant. Answer the following question: {{question}}",
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}

@AssertToxicity(
    threshold = 0.7,
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}

@AssertRelevance(
    threshold = 0.7,
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}

@AssertPromptInjection(
    threshold = 0.7,
)
public void testAddFunctionality(String question) {
    String result = addFunctionality(question);
    assertNotNull(result);
}

@Metric(
    metric=MetricEnum.FAITHFULNESS,
    threshold=0.7,
    key="functionality",
    type="string"
)

