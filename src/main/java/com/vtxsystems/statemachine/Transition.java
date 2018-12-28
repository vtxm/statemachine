package com.vtxsystems.statemachine;

public class Transition<S, E> {
    private final S newState;
    private final TransitionListener<S, E> onTransition;

    public Transition(S newState) {
        this(newState, null);
    }

    public Transition(S newState, TransitionListener<S, E> onTransition) {
        this.newState = newState;
        this.onTransition = onTransition;
    }

    public S getNewState() {
        return newState;
    }

    public TransitionListener<S, E> getOnTransition() {
        return onTransition;
    }
}
