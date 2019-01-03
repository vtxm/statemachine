package com.vtxsystems.statemachine;

public class UncheckedUnexpectedEventException extends RuntimeException {
    public UncheckedUnexpectedEventException(UnexpectedEventException e) {
        super(e);
    }
}
