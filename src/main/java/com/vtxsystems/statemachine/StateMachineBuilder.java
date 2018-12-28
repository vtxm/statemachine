package com.vtxsystems.statemachine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StateMachineBuilder<S, E> {
    StateMachineBuilder<S, E> setInitialState(S state);

    StateMachineBuilder<S, E> addState(S state);

    StateMachineBuilder<S, E> addState(S state, Consumer<E> defaultProcessor);

    StateMachineBuilder<S, E> addFinalStates(S... states);

    StateMachineBuilder<S, E> addTransition(E event, S state, S newState);

    StateMachineBuilder<S, E> addTransition(E event, S state, Transition<S, E> transition);

    StateMachineBuilder<S, E> addOnBeginWorkCallback(Consumer<E> onBeginWork);

    StateMachineBuilder<S, E> addOnEndWorkCallback(Consumer<List<E>> onEndWork);

    StateMachineBuilder<S, E> addOnUnexpectedEventCallback(BiConsumer<S, List<E>> onUnexpectedEvent);

    StateMachine<S, E> build();
}
