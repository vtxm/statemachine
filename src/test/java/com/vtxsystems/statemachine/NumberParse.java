package com.vtxsystems.statemachine;

import org.junit.Test;

import java.util.List;

import static com.vtxsystems.statemachine.CharacterHelpers.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

public class NumberParse {
    private static <E> void swallowException(StateMachine sm, E e) {
        try {
            sm.process(e);
        } catch (UnexpectedEventException ex) { }
    }

    StateMachine<Integer, Character> digit = new SimpleStateMachineBuilder<Integer, Character>()
            .setInitialState(0)
            .addState(0)
            .addState(1)
            .addFinalStates(1)
            .addTransition(0, DIGIT_CHARS, 1)
            .build();

    StateMachine<Integer, Integer> empty = new SimpleStateMachineBuilder<Integer, Integer>()
            .setInitialState(0)
            .addState(0)
            .addFinalStates(0)
            .build();

    StateMachine<StateMachine, Character> numberRepetition1 = new SimpleStateMachineBuilder<StateMachine, Character>()
            .setInitialState(digit)
            .addState(digit, e -> swallowException(digit, e), null)
            .addFinalStates(empty)
            .addTransition(digit, e -> digit.isFinishedSuccessfully(), new Transition<>(digit, (o, s, e) -> digit.reset()))
            .addTransition(digit, e -> digit.isFinishedWithError(), empty)
            .build();

    StateMachine<StateMachine, Character> number = new SimpleStateMachineBuilder<StateMachine, Character>()
            .setInitialState(digit)
            .addState(digit, e -> swallowException(digit, e), null)
            .addState(numberRepetition1, e -> swallowException(numberRepetition1, e), null)
            .addFinalStates(empty)
            .addTransition(digit, e -> digit.isFinished(), new Transition<>(numberRepetition1, (o, s, e) -> digit.reset()))
            .addTransition(numberRepetition1, e -> numberRepetition1.isFinished(), empty)
            .addTransition(numberRepetition1, e -> !numberRepetition1.isFinished(), numberRepetition1)
            .addOnEndWorkCallback(chars -> System.out.println(charListToString(chars)))
            .build();

    @Test
    public void test() {
        List<Character> correct = "123\0".chars().mapToObj(e -> (char) e).collect(toList());
        number.processEvents(correct);
        assertTrue(number.isFinishedSuccessfully());
    }
}
