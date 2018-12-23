package com.vtxsystems.statemachine;

@FunctionalInterface
public interface StateChangeListener<S, E> {
    void accept(S oldState, S newState, E event);
}
