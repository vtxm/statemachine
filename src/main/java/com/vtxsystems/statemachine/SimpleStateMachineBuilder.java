package com.vtxsystems.statemachine;

import com.vtxsystems.statemachine.internal.ConditionalTransition;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SimpleStateMachineBuilder<S, E> implements StateMachineBuilder<S, E> {
    final Map<S, Map<E, Transition<S, E>>> states = new HashMap<>();
    final Map<S, List<ConditionalTransition<S, E>>> conditionalTransitions = new HashMap<>();
    final Set<S> finalStates = new HashSet<>();
    final Map<S, Consumer<E>> defaultPostProcessors = new HashMap<>();
    final Map<S, Consumer<E>> defaultPreProcessors = new HashMap<>();
    final List<Consumer<E>> onBeginWork = new ArrayList<>();
    final List<Consumer<List<E>>> onEndWork = new ArrayList<>();
    final List<Consumer<UnexpectedEventException>> onUnexpectedEvent = new ArrayList<>();
    S initialState = null;

    @Override
    public StateMachineBuilder<S, E> setInitialState(S state) {
        initialState = state;
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addState(S state,
                                              Consumer<E> defaultPreProcessor,
                                              Consumer<E> defaultPostProcessor) {
        if (states.containsKey(state)) {
            throw new IllegalArgumentException("State machine already contains this state: " + state.toString());
        }
        states.put(state, new HashMap<>());
        defaultPreProcessors.put(state, defaultPreProcessor);
        defaultPostProcessors.put(state, defaultPostProcessor);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addFinalStates(S... states) {
        for (S state : states) {
            this.states.put(state, new HashMap<>());
            finalStates.add(state);
        }
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addTransition(S state, E event, Transition<S, E> transition) {
        Map<E, Transition<S, E>> transitions = states.get(state);
        if (transitions == null) {
            throw new IllegalArgumentException("No such state: " + state.toString());
        }
        if (!states.containsKey(transition.getNewState())) {
            throw new IllegalArgumentException("No such state: " + transition.getNewState());
        }
        transitions.put(event, transition);
        return this;
    }

    @Override
    public StateMachineBuilder<S, E> addTransition(S state, Predicate<E> condition, Transition<S, E> transition) {
        List<ConditionalTransition<S, E>> list = conditionalTransitions.get(state);
        if (list == null) {
            list = new ArrayList<>();
            conditionalTransitions.put(state, list);
        }
        list.add(new ConditionalTransition<>(condition, transition));
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
    public StateMachineBuilder<S, E> addOnUnexpectedEventCallback(Consumer<UnexpectedEventException> onUnexpectedEvent) {
        this.onUnexpectedEvent.add(onUnexpectedEvent);
        return this;
    }

    @Override
    public StateMachine<S, E> build() {
        if (finalStates.size() == 0) {
            throw new RuntimeException("There are no final states specified.");
        }
        return new SimpleStateMachine<>(this);
    }
}
