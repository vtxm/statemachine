package com.vtxsystems.statemachine.internal;

import com.vtxsystems.statemachine.Transition;

import java.util.function.Predicate;

public class ConditionalTransition<S, E> {
    private final Predicate<E> condition;
    private final Transition<S, E> transition;

    public ConditionalTransition(Predicate<E> condition, Transition<S, E> transition) {
        this.condition = condition;
        this.transition = transition;
    }

    public Predicate<E> getCondition() {
        return condition;
    }

    public Transition<S, E> getTransition() {
        return transition;
    }
}
