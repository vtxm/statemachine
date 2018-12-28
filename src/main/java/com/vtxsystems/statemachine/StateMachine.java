package com.vtxsystems.statemachine;

import java.util.List;

public interface StateMachine<S, E> {
    enum MachineState {
        NOT_STARTED,
        WORKING,
        FINISHED
    }

    S process(E event);

    default void processEvents(List<E> event) {
        event.forEach(e -> process(e));
    }
}
