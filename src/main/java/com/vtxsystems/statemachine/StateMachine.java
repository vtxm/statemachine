package com.vtxsystems.statemachine;

import java.util.List;

import static com.vtxsystems.statemachine.StateMachine.MachineState.*;

public interface StateMachine<S, E> {
    enum MachineState {
        NOT_STARTED,
        WORKING,
        FINISHED_SUCCESSFULLY,
        FINISHED_WITH_ERROR
    }

    default S silentProcess(E event) {
        try {
            return process(event);
        } catch (UnexpectedEventException e) {
            throw new UncheckedUnexpectedEventException(e);
        }
    }

    S process(E event) throws UnexpectedEventException;

    default void processEvents(List<E> events) {
        try {
            for (E e : events) {
                process(e);
            }
        } catch (UnexpectedEventException ex) {
            throw new RuntimeException(ex);
        }
    }

    MachineState getMachineState();

    default boolean isFinished() {
        return getMachineState() == FINISHED_SUCCESSFULLY || getMachineState() == FINISHED_WITH_ERROR;
    }

    default boolean isFinishedSuccessfully() {
        return getMachineState() == FINISHED_SUCCESSFULLY;
    }

    default boolean isFinishedWithError() {
        return getMachineState() == FINISHED_WITH_ERROR;
    }

    default boolean isWorking() {
        return getMachineState() == WORKING;
    }

    void reset();
}
