package com.vtxsystems.statemachine;

import java.util.List;

public class UnexpectedEventException extends Exception {
    private final Object state;
    private final Object event;
    private final List<Object> eventHistory;

    public UnexpectedEventException(Object state, Object event, List<Object> eventHistory) {
        super("Unexpected event: " + event.toString());
        this.state = state;
        this.event = event;
        this.eventHistory = eventHistory;
    }

    public Object getState() {
        return state;
    }

    public Object getEvent() {
        return event;
    }

    public List<Object> getEventHistory() {
        return eventHistory;
    }
}
