package com.vtxsystems.statemachine;

@FunctionalInterface
public interface TransitionListener<S, E> {
    void accept(S oldState, S newState, E event);
}
