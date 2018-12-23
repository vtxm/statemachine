package com.vtxsystems.statemachine;

public class StateChange<S, E> {
    private final S newState;
    private final StateChangeListener<S, E> onStateChange;

    public StateChange(S newState) {
        this(newState, null);
    }

    public StateChange(S newState, StateChangeListener<S, E> onStateChange) {
        this.newState = newState;
        this.onStateChange = onStateChange;
    }

    public S getNewState() {
        return newState;
    }

    public StateChangeListener<S, E> getOnStateChange() {
        return onStateChange;
    }
}
