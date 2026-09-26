# food-scanner example

A complete `@SpringBootTest` end-to-end example of `llmunit`: a small Spring Boot service
(`FoodScanner`) whose output is asserted against LLM evaluators via the "bring-your-own-output"
model.

This mirrors the canonical usage (see `references/suggestions.md`):

```java
@SpringBootTest
@ExtendWith(LLMTestExtension.class)
class FoodScannerTest {
    @Autowired FoodScanner scanner;
    @Autowired ChatClient.Builder judge;

    @LLMTest(trials = 5, passRate = 0.8) // 4 out of 5 calls must pass
    void testFoodScanner() {
        String answer = scanner.suggest("coca cola");
        assertThatLLM(answer)
            .withQuery("a cleaner alternative to Coca Cola")
            .withContext(retrievedProductDocs)
            .passesEval(new FactCheckingEval(judge))
            .passesEval(new RelevanceEval(judge));
    }
}
```

## Prerequisites

- Java 21, Maven.
- `llmunit` installed in your local Maven repository:

  ```bash
  cd ../..           # repository root
  mvn -q install
  ```

## Run the tests

```bash
mvn test
```

### What's happening

- `@SpringBootTest` boots the whole application context, so `FoodScanner` and the
  `ChatClient.Builder` judge bean are autowired exactly as they would be in your app.
- The test body calls the application's own `scanner.suggest(...)` to get a `String` — llmunit
  never generates the output.
- `assertThatLLM(answer).withQuery(...).withContext(...).passesEval(...)` records one result per
  eval; the extension aggregates pass/fail across the 5 `trials` and fails only below
  `passRate = 0.8`.
- The judge here is a `DummyChatModel` so the example runs offline with no API keys. To judge
  real output, add a Spring AI provider starter and configure it (see
  `src/main/resources/application.properties`), then switch to `FactCheckingEval`/`RelevanceEval`.

## Offline (CI) mode

Once you have golden results for this test class, you can run it without contacting any model:

```bash
# recording run, needs a real judge
mvn -Dllmunit.recordGolden=true test

# deterministic replay in CI
mvn -Dllmunit.offline=true test
```

Golden files are written to `src/test/resources/llmunit-records/<ClassName>.json` (commit them).
See the repository's `USAGE.md` for the full offline workflow.