package io.llmunit;

import io.llmunit.annotations.LLMTest;
import io.llmunit.core.Eval;
import io.llmunit.core.EvalInput;
import io.llmunit.core.EvalResult;
import io.llmunit.core.LLMAssert;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class TrialsTest {

    private static final AtomicInteger TRIAL = new AtomicInteger();

    @LLMTest(trials = 4, passRate = 0.5)
    void toleratesFlakyTrials() {
        int i = TRIAL.getAndIncrement();
        LLMAssert.assertThatLLM("a")
            .withQuery("q")
            .passesEval(i % 2 == 0 ? passing() : failing());
    }

    @LLMTest(trials = 4, passRate = 0.75)
    void failsOneOfFourButPassesRate() {
        int i = TRIAL.getAndIncrement();
        LLMAssert.assertThatLLM("a")
            .withQuery("q")
            .passesEval(i % 4 == 3 ? failing() : passing());
    }

    private static Eval passing() {
        return new StubEval("pass", 1.0);
    }

    private static Eval failing() {
        return new StubEval("fail", 0.0);
    }

    private static final class StubEval implements Eval {

        private final String name;
        private final double score;

        StubEval(String name, double score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public double threshold() {
            return 0.5;
        }

        @Override
        public EvalResult evaluate(EvalInput input) {
            return new EvalResult(name, score, 0.5, name);
        }
    }
}