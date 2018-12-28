package com.vtxsystems.statemachine;

import org.junit.Test;

import static com.vtxsystems.statemachine.CharacterHelpers.charListToString;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class SimpleStateMachineTest {
    @Test
    public void test() {
        StringBuilder acceptedText = new StringBuilder();
        StringBuilder unexpected = new StringBuilder();
        StateMachineBuilder<Integer, Character> builder = new SimpleStateMachineBuilder<Integer, Character>()
                .setInitialState(0)

                .addState(0)
                .addState(1)
                .addState(2)

                .addTransition('a', 0, 1)
                .addTransition('s', 1, 1)
                .addTransition('d', 1, 1)
                .addTransition(' ', 1, 2)
                .addTransition('!', 2, -1)

                .addFinalStates(-1)

                .addOnBeginWorkCallback(c -> System.out.println("First char: " + c))
                .addOnEndWorkCallback(lc -> acceptedText.append(charListToString(lc)))
                .addOnUnexpectedEventCallback((i, lc) -> unexpected.append(lc.get(lc.size() - 1)));

        builder.build().processEvents("asd !".chars().mapToObj(e -> (char) e).collect(toList()));
        assertEquals("asd !", acceptedText.toString());
        try {
            builder.build().processEvents("asd 1".chars().mapToObj(e -> (char) e).collect(toList()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertEquals("1", unexpected.toString());
    }
}
