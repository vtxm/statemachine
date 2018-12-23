package com.vtxsystems.statemachine;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface StateMachine<S, E> {
    StateMachine setInitialState(S state);
    StateMachine addState(S state, Consumer<E> defaultProcessor);
    StateMachine addStateChange(S state, Predicate<E> predicate, StateChange<S, E> stateChange);
    StateMachine addOnBeginWorkCallback(Consumer<E> onBeginWork);
    boolean lastStatesAre(S... lastStates);
    boolean lastEventsAre(E... lastEvents);
    S process(E event);
}
