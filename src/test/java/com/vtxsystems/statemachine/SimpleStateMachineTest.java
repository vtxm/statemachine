package com.vtxsystems.statemachine;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.vtxsystems.statemachine.CharacterHelpers.charListToString;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleStateMachineTest {
    @Test
    public void test() {
        StringBuilder acceptedText = new StringBuilder();
        StringBuilder unexpected = new StringBuilder();
        AtomicInteger beginWorks = new AtomicInteger(0);
        StateMachineBuilder<Integer, Character> builder = new SimpleStateMachineBuilder<Integer, Character>()
                .addState(0)
                .addState(1)
                .addState(2)

                .addFinalStates(-1)

                .addTransition(0, 'a', 1)
                .addTransition(1, 's', 1)
                .addTransition(1, 'd', 1)
                .addTransition(1, ' ', 2)
                .addTransition(2, '!', -1)

                .setInitialState(0)

                .addOnBeginWorkCallback(c -> beginWorks.set(1))
                .addOnEndWorkCallback(lc -> acceptedText.append(charListToString(lc)))
                .addOnUnexpectedEventCallback(ex -> unexpected.append(ex.getEvent()));

        builder.build().processEvents("asd !".chars().mapToObj(e -> (char) e).collect(toList()));
        assertEquals("asd !", acceptedText.toString());
        try {
            builder.build().processEvents("asd 1".chars().mapToObj(e -> (char) e).collect(toList()));
        } catch (Exception e) {
        }
        assertEquals("1", unexpected.toString());
        assertEquals(1, beginWorks.get());
    }

    @Test
    public void conditionalTest() {
        AtomicInteger itWorks = new AtomicInteger(0);
        StateMachineBuilder<Integer, Integer> builder = new SimpleStateMachineBuilder<Integer, Integer>()
                .addState(0)
                .addState(1)
                .addState(2)

                .addFinalStates(-1)

                .addTransition(0, 1, 1)
                .addTransition(1, i -> i > 5, 2)
                .addTransition(2, 2, -1)

                .setInitialState(0)

                .addOnEndWorkCallback(li -> itWorks.set(1));

        builder.build().processEvents(Arrays.asList(1, 6, 2));
        assertEquals(1, itWorks.get());
    }

    @Test
    public void emptyTest() {
        StateMachine<Integer, Integer> empty = new SimpleStateMachineBuilder<Integer, Integer>()
                .setInitialState(0)
                .addFinalStates(0)
                .build();
        assertTrue(empty.isFinished());
    }
}
