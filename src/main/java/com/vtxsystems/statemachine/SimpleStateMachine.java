package com.vtxsystems.statemachine;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SimpleStateMachine<S, E> implements StateMachine<S, E> {
    public static final int MAX_LAST = 16;

    private final Map<S, Map<Predicate<E>, StateChange<S, E>>> states = new LinkedHashMap<>();
    private final Map<S, Consumer<E>> defaultProcessors = new HashMap<>();
    private final List<S> lastStates = new LinkedList<>();
    private final List<E> lastEvents = new LinkedList<>();
    private final List<Consumer<E>> onBeginWork = new ArrayList<>();
    private S initialState = null;
    private S currentState = null;

    @Override
    public StateMachine setInitialState(S state) {
        initialState = state;
        return this;
    }

    @Override
    public StateMachine addState(S state, Consumer<E> defaultProcessor) {
        states.put(state, new LinkedHashMap<>());
        defaultProcessors.put(state, defaultProcessor);
        return this;
    }

    @Override
    public StateMachine addStateChange(S state, Predicate<E> predicate, StateChange<S, E> stateChange) {
        states.get(state).put(predicate, stateChange);
        return this;
    }

    @Override
    public StateMachine addOnBeginWorkCallback(Consumer<E> onBeginWork) {
        this.onBeginWork.add(onBeginWork);
        return this;
    }

    private boolean compareListTails(List list, List toCompareWith) {
        if (toCompareWith.size() > MAX_LAST) {
            throw new RuntimeException("Array size exceeds maximum: " + String.valueOf(MAX_LAST));
        }
        for (int i = 0; i < toCompareWith.size(); i++) {
            if (!toCompareWith.get(toCompareWith.size() - i - 1).equals(list.get(list.size() - i -1))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean lastStatesAre(S... lastStates) {
        List<S> ls = Arrays.asList(lastStates);
        return compareListTails(this.lastStates, ls);
    }

    @Override
    public boolean lastEventsAre(E... lastEvents) {
        List<E> ls = Arrays.asList(lastEvents);
        return compareListTails(this.lastEvents, ls);
    }

    private void init(E event) {
        currentState = initialState;
        if (currentState == null) {
            throw new RuntimeException("Initial state is not set. Use setInitialState(state) method.");
        }
        onBeginWork.forEach(c -> c.accept(event));
    }

    private void addLastEvent(E event) {
        lastEvents.add(event);
        if (lastEvents.size() > MAX_LAST) {
            lastEvents.remove(0);
        }
    }

    private void addLastState(S state) {
        lastStates.add(state);
        if (lastStates.size() > MAX_LAST) {
            lastStates.remove(0);
        }
    }

    @Override
    public S process(E event) {
        if (currentState == null) {
            init(event);
        }
        Map<Predicate<E>, StateChange<S, E>> possibleChanges = states.get(currentState);
        S newState = null;
        for (Map.Entry<Predicate<E>, StateChange<S, E>> e : possibleChanges.entrySet()) {
            if (e.getKey().test(event)) {
                StateChange<S, E> stateChange = e.getValue();
                newState = stateChange.getNewState();
                StateChangeListener<S, E> callback = stateChange.getOnStateChange();
                if (callback != null) {
                    callback.accept(currentState, newState, event);
                }
                break;
            }
        }
        currentState = newState == null ? currentState : newState;
        Consumer<E> defaultProcessor = defaultProcessors.get(currentState);
        if (defaultProcessor != null) {
            defaultProcessor.accept(event);
        }
        addLastEvent(event);
        addLastState(currentState);
        return currentState;
    }
}
