package com.vtxsystems.statemachine;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleStateMachine<S, E> implements StateMachine<S, E> {
    private final Map<S, Map<E, Transition<S, E>>> states = new LinkedHashMap<>();
    private final Set<S> finalStates = new HashSet<>();
    private final Map<S, Consumer<E>> defaultProcessors = new HashMap<>();
    private final List<Consumer<E>> onBeginWork = new ArrayList<>();
    private final List<Consumer<List<E>>> onEndWork = new ArrayList<>();
    private final List<BiConsumer<S, List<E>>> onUnexpectedEvent = new ArrayList<>();
    private final S initialState;
    private S currentState;
    private MachineState machineState = MachineState.NOT_STARTED;
    private final List<E> eventsHistory = new ArrayList<>();

    public SimpleStateMachine(SimpleStateMachineBuilder<S, E> stateMachineBuilder) {
        states.putAll(stateMachineBuilder.states);
        initialState = stateMachineBuilder.initialState;
        if (!states.containsKey(initialState)) {
            throw new RuntimeException("Set of states does not contain specified initial state.");
        }
        finalStates.addAll(stateMachineBuilder.finalStates);
        if (finalStates.size() == 0) {
            throw new RuntimeException("There are no final states specified.");
        }
        defaultProcessors.putAll(stateMachineBuilder.defaultProcessors);
        onBeginWork.addAll(stateMachineBuilder.onBeginWork);
        onEndWork.addAll(stateMachineBuilder.onEndWork);
        onUnexpectedEvent.addAll(stateMachineBuilder.onUnexpectedEvent);
        currentState = initialState;
    }

    @Override
    public S process(E event) {
        if (machineState == MachineState.NOT_STARTED) {
            machineState = MachineState.WORKING;
            onBeginWork.forEach(c -> c.accept(event));
        } else if (machineState == MachineState.FINISHED) {
            throw new IllegalStateException("This state machine already finished work.");
        }
        eventsHistory.add(event);
        Map<E, Transition<S, E>> transitions = states.get(currentState);
        Transition<S, E> transition = transitions.get(event);
        S newState;
        if (transition == null) {
            machineState = MachineState.FINISHED;
            onUnexpectedEvent.forEach(c -> c.accept(currentState, eventsHistory));
            throw new RuntimeException("Unexpected event: " + event.toString());
        } else {
            newState = transition.getNewState();
            TransitionListener<S, E> callback = transition.getOnTransition();
            if (callback != null) {
                callback.accept(currentState, newState, event);
            }
        }
        currentState = newState;
        if (finalStates.contains(currentState)) {
            machineState = MachineState.FINISHED;
            onEndWork.forEach(c -> c.accept(eventsHistory));
        }
        Consumer<E> defaultProcessor = defaultProcessors.get(currentState);
        if (defaultProcessor != null) {
            defaultProcessor.accept(event);
        }
        return currentState;
    }
}
