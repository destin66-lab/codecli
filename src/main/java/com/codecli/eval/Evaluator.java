package com.codecli.eval;

@FunctionalInterface
public interface Evaluator {
    EvalReport evaluate(EvalCase expected, EvalObservation actual);
}
