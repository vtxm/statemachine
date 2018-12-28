package com.vtxsystems.statemachine;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleStateMachineBuilder<S, E> implements StateMachineBuilder<S, E> {
    final Map<S, Map<E, Transition<S, E>>> states = new LinkedHashMap<>();
    final Set<S> finalStates = new HashSet<>();
    final Map<S, Consumer<E>> defaultProcessors = new HashMap<>();
    final List<Consumer<E>> onBeginWork = new ArrayList<>();
    final List<Consumer<List<E>>> onEndWork = new ArrayList<>();
    final List<BiConsumer<S, List<E>>> onUnexpectedEvent = new ArrayList<>();
    S initialState = null;

    @Override
    public StateMachineBuilder<S, E> setInitialState(S state) {
        initialState = state;
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addState(S state) {
        if (states.containsKey(state)) {
            throw new IllegalArgumentException("State machine already contains this state: " + state.toString());
        }
        states.put(state, new HashMap<>());
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addState(S state, Consumer<E> defaultProcessor) {
        addState(state);
        defaultProcessors.put(state, defaultProcessor);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addFinalStates(S... states) {
        for (S state : states) {
            addState(state);
            finalStates.add(state);
        }
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addTransition(E event, S state, S newState) {
        return addTransition(event, state, new Transition<>(newState, null));
    }

    @Override
    public StateMachineBuilder<S, E> addTransition(E event, S state, Transition<S, E> transition) {
        Map<E, Transition<S, E>> transitions = states.get(state);
        if (transitions == null) {
            throw new IllegalArgumentException("No such state: " + state.toString());
        }
        transitions.put(event, transition);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addOnBeginWorkCallback(Consumer<E> onBeginWork) {
        this.onBeginWork.add(onBeginWork);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addOnEndWorkCallback(Consumer<List<E>> onEndWork) {
        this.onEndWork.add(onEndWork);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addOnUnexpectedEventCallback(BiConsumer<S, List<E>> onUnexpectedEvent) {
        this.onUnexpectedEvent.add(onUnexpectedEvent);
        return this;
    }

    @Override
    public StateMachine<S, E> build() {
        return new SimpleStateMachine<>(this);
    }
}
