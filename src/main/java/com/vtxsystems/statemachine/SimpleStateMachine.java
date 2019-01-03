package com.vtxsystems.statemachine;

import com.vtxsystems.statemachine.internal.ConditionalTransition;

import java.util.*;
import java.util.function.Consumer;

import static com.vtxsystems.statemachine.StateMachine.MachineState.*;
import static com.vtxsystems.statemachine.StateMachine.MachineState.FINISHED_SUCCESSFULLY;
import static com.vtxsystems.statemachine.StateMachine.MachineState.FINISHED_WITH_ERROR;

public class SimpleStateMachine<S, E> implements StateMachine<S, E> {
    private final Map<S, Map<E, Transition<S, E>>> states = new LinkedHashMap<>();
    private final Map<S, List<ConditionalTransition<S, E>>> conditionalTransitions = new HashMap<>();
    private final Set<S> finalStates = new HashSet<>();
    private final Map<S, Consumer<E>> defaultPostProcessors = new HashMap<>();
    private final Map<S, Consumer<E>> defaultPreProcessors = new HashMap<>();
    private final List<Consumer<E>> onBeginWork = new ArrayList<>();
    private final List<Consumer<List<E>>> onEndWork = new ArrayList<>();
    private final List<Consumer<UnexpectedEventException>> onUnexpectedEvent = new ArrayList<>();
    private final S initialState;

    private S currentState;
    private MachineState machineState;
    private final List<E> eventsHistory = new ArrayList<>();

    public SimpleStateMachine(SimpleStateMachineBuilder<S, E> stateMachineBuilder) {
        states.putAll(stateMachineBuilder.states);
        conditionalTransitions.putAll(stateMachineBuilder.conditionalTransitions);
        initialState = stateMachineBuilder.initialState;
        if (!states.containsKey(initialState)) {
            throw new RuntimeException("Set of states does not contain specified initial state.");
        }
        finalStates.addAll(stateMachineBuilder.finalStates);
        defaultPreProcessors.putAll(stateMachineBuilder.defaultPreProcessors);
        defaultPostProcessors.putAll(stateMachineBuilder.defaultPostProcessors);
        onBeginWork.addAll(stateMachineBuilder.onBeginWork);
        onEndWork.addAll(stateMachineBuilder.onEndWork);
        onUnexpectedEvent.addAll(stateMachineBuilder.onUnexpectedEvent);
        currentState = initialState;
        machineState = finalStates.contains(currentState) ? FINISHED_SUCCESSFULLY : NOT_STARTED;
    }

    private void finishWithError(E event) throws UnexpectedEventException {
        machineState = FINISHED_WITH_ERROR;
        UnexpectedEventException ex = new UnexpectedEventException(currentState,
                event,
                (List<Object>) eventsHistory
        );
        onUnexpectedEvent.forEach(c -> c.accept(ex));
        throw ex;
    }

    private S tryConditionalTransition(E event) {
        List<ConditionalTransition<S, E>> list = conditionalTransitions.get(currentState);
        if (list != null) {
            for (ConditionalTransition<S, E> ct : list) {
                if (ct.getCondition().test(event)) {
                    TransitionListener<S, E> callback = ct.getTransition().getOnTransition();
                    if (callback != null) {
                        callback.accept(currentState, ct.getTransition().getNewState(), event);
                    }
                    return ct.getTransition().getNewState();
                }
            }
        }
        return null;
    }

    private S toNewState(E event) throws UnexpectedEventException {
        Map<E, Transition<S, E>> transitions = states.get(currentState);
        if (transitions == null) {
            finishWithError(event);
        }
        Transition<S, E> transition = transitions.get(event);
        S newState;
        if (transition == null) {
            newState = tryConditionalTransition(event);
            if (newState == null) {
                finishWithError(event);
            }
        } else {
            newState = transition.getNewState();
            TransitionListener<S, E> callback = transition.getOnTransition();
            if (callback != null) {
                callback.accept(currentState, newState, event);
            }
        }
        return newState;
    }

    @Override
    public S process(E event) throws UnexpectedEventException {
        if (machineState == MachineState.NOT_STARTED) {
            machineState = finalStates.contains(initialState) ? FINISHED_SUCCESSFULLY : WORKING;
            onBeginWork.forEach(c -> c.accept(event));
        } else if (machineState == FINISHED_SUCCESSFULLY || machineState == FINISHED_WITH_ERROR) {
            throw new IllegalStateException("This state machine already finished work.");
        }
        Consumer<E> defaultPreProcessor = defaultPreProcessors.get(currentState);
        if (defaultPreProcessor != null) {
            defaultPreProcessor.accept(event);
        }
        eventsHistory.add(event);
        currentState = toNewState(event);
        if (finalStates.contains(currentState)) {
            machineState = FINISHED_SUCCESSFULLY;
            onEndWork.forEach(c -> c.accept(eventsHistory));
        }
        Consumer<E> defaultPostProcessor = defaultPostProcessors.get(currentState);
        if (defaultPostProcessor != null) {
            defaultPostProcessor.accept(event);
        }
        return currentState;
    }

    @Override
    public MachineState getMachineState() {
        return machineState;
    }

    @Override
    public void reset() {
        currentState = initialState;
        machineState = NOT_STARTED;
        eventsHistory.clear();
    }
}
