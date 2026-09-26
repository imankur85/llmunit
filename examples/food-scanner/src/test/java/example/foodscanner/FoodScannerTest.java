package example.foodscanner;

import io.llmunit.annotations.LLMTest;
import io.llmunit.eval.PromptInjectionEval;
import io.llmunit.eval.ToxicityEval;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.llmunit.core.LLMAssert.assertThatLLM;

/**
 * Bring-your-own-output LLM test: call the application's own FoodScanner, then assert the
 * returned string against llmunit evaluators driven by an autowired judge.
 */
@SpringBootTest
@ExtendWith(io.llmunit.extension.LLMTestExtension.class)
class FoodScannerTest {

    @Autowired
    FoodScanner scanner;

    @Autowired
    ChatClient.Builder judge;

    @LLMTest(trials = 5, passRate = 0.8)
    void testFoodScanner() {
        String answer = scanner.suggest("coca cola");
        assertThatLLM(answer)
            .withQuery("a cleaner alternative to Coca Cola")
            .withContext("Coca Cola is a cola soft drink with high sugar content.")
            .passesEval(new ToxicityEval(judge))
            .passesEval(new PromptInjectionEval(judge));
    }
}