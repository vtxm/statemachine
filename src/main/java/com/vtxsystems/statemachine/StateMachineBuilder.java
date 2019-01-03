package com.vtxsystems.statemachine;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface StateMachineBuilder<S, E> {
    StateMachineBuilder<S, E> setInitialState(S state);

    default StateMachineBuilder<S, E> addState(S state) {
        return addState(state, null, null);
    }

    default StateMachineBuilder<S, E> addState(S state, Consumer<E> defaultPostProcessor) {
        return addState(state, null, defaultPostProcessor);
    }

    StateMachineBuilder<S, E> addState(S state, Consumer<E> defaultPreProcessor, Consumer<E> defaultPostProcessor);

    StateMachineBuilder<S, E> addFinalStates(S... states);

    StateMachineBuilder<S, E> addTransition(S state, E event, Transition<S, E> transition);

    default StateMachineBuilder<S, E> addTransition(S state, E event, S newState) {
        return addTransition(state, event, new Transition<>(newState, null));
    }

    default StateMachineBuilder<S, E> addTransition(S state, Collection<E> events, S newState) {
        for (E e : events) {
            addTransition(state, e, newState);
        }
        return this;
    }

    default StateMachineBuilder<S, E> addTransition(S state, Collection<E> events, Transition<S, E> transition) {
        for (E e : events) {
            addTransition(state, e, transition);
        }
        return this;
    }

    default StateMachineBuilder<S, E> addTransition(S state, Predicate<E> condition, S newState) {
        return addTransition(state, condition, new Transition<S, E>(newState));
    }

    StateMachineBuilder<S, E> addTransition(S state, Predicate<E> condition, Transition<S, E> transition);

    StateMachineBuilder<S, E> addOnBeginWorkCallback(Consumer<E> onBeginWork);

    StateMachineBuilder<S, E> addOnEndWorkCallback(Consumer<List<E>> onEndWork);

    StateMachineBuilder<S, E> addOnUnexpectedEventCallback(Consumer<UnexpectedEventException> onUnexpectedEvent);

    StateMachine<S, E> build();
}
