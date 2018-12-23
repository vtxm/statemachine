package com.vtxsystems.statemachine;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.States.INSIDE_WORD;
import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.States.INSIDE_COMMENT;
import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.States.INSIDE_SPACE;
import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.States.INSIDE_STRING;

public class SimpleStateMachineTextTest {
    enum States {
        INSIDE_WORD,
        INSIDE_COMMENT,
        INSIDE_SPACE,
        INSIDE_STRING
    }

    private static final char[] wordChars = {
            ':',
            '"',
            '<',
            '>',
            '+',
            '-',
            '*',
            '/',
            '\\',
            '[',
            ']',
            '{',
            '}',
            '(',
            ')',
            ';'
    };

    private static final char[] spaceChars = { ' ', '\n', '\t', '\u001a' };

    StringBuilder accumulator = null;
    List<SourceWord> words = new ArrayList<>();

    enum SourceWordType {
        INSTRUCTION,
        STRING,
        INTNUMBER,
        FLOATNUMBER
    }

    class SourceWord {
        private final SourceWordType sourceWordType;
        private final String value;

        public SourceWord(SourceWordType sourceWordType, String value) {
            this.sourceWordType = sourceWordType;
            this.value = value;
        }

        public SourceWordType getSourceWordType() {
            return sourceWordType;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SourceWord that = (SourceWord) o;
            return sourceWordType == that.sourceWordType &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceWordType, value);
        }

        @Override
        public String toString() {
            return value + " (" + sourceWordType.toString() + ")";
        }
    }

    private void init(Object e) {
        accumulator = new StringBuilder();
    }

    private static boolean charBetween(Object c, char a, char b) {
        Character cc = (Character) c;
        return cc.charValue() >= a && cc.charValue() <= b;
    }

    private static boolean charIn(Object c, char[] chars) {
        boolean res = false;
        Character cc = (Character) c;
        for (char charr : chars) {
            if (cc.equals(charr)) {
                res = true;
                break;
            }
        }
        return res;
    }

    private static boolean charIs(Object c, char a) {
        Character cc = (Character) c;
        return cc.equals(a);
    }

    private SourceWord newWord(String s, States state) {
        SourceWordType swt = null;
        if (s.matches("[0-9]{1,10}")) {
            swt = SourceWordType.INTNUMBER;
        } else if (s.matches("([0-9]|\\.){1,10}")) {
            swt = SourceWordType.FLOATNUMBER;
        } else {
            if (state == INSIDE_WORD) {
                swt = SourceWordType.INSTRUCTION;
            } else if (state == INSIDE_STRING) {
                swt = SourceWordType.STRING;
                s = s.substring(1);
            }
        }
        return new SourceWord(swt, s);
    }

    private void accumulate(Object oldState, Object state, Object e) {
        States os = (States) oldState;
        words.add(newWord(accumulator.toString(), os));
        accumulator = new StringBuilder();
        States s = (States) state;
        if (s == INSIDE_WORD || s == States.INSIDE_STRING) {
            accumulator.append(e);
        }
    }

    private void startComment() {
        accumulator = new StringBuilder();
    }

    private boolean isWordChar(Object c) {
        Character cc = (Character) c;
        return charBetween(cc, 'A', 'Z') || charBetween(cc, 'a', 'z')
                || charBetween(cc, '0', '9') || charIn(cc, wordChars);
    }

    @Test
    public void textSimpleTest() {
        StateMachine<States, Character> stateMachine = new SimpleStateMachine<>();
        stateMachine
            .setInitialState(INSIDE_SPACE)
            .addOnBeginWorkCallback(e -> init(e))

            .addState(INSIDE_SPACE, null)
            .addState(INSIDE_COMMENT, null)
            .addState(INSIDE_WORD, e -> accumulator.append(e))
            .addState(INSIDE_STRING, e -> accumulator.append(e))

            .addStateChange(INSIDE_SPACE, c -> charIs(c, '"'), new StateChange(INSIDE_STRING))
            .addStateChange(INSIDE_SPACE, c -> isWordChar(c), new StateChange(INSIDE_WORD))

            .addStateChange(INSIDE_COMMENT, c -> charIs(c, '\n'), new StateChange(INSIDE_SPACE))

            .addStateChange(INSIDE_WORD, c -> charIs(c, '/') && stateMachine.lastEventsAre('/'),
                    new StateChange(INSIDE_COMMENT, (o, s, e) -> startComment()))
            .addStateChange(INSIDE_WORD, c -> charIn(c, spaceChars),
                    new StateChange(INSIDE_SPACE, (o, s, e) -> accumulate(o ,s, e)))

            .addStateChange(INSIDE_STRING, c -> charIs(c, '"') && !stateMachine.lastEventsAre('\\'),
                    new StateChange(INSIDE_SPACE, (o, s, e) -> accumulate(o ,s, e)));

        String source = "aaa \"ss\\\" z\"  123 1.2 // cc \nii:./uu";
        source = source.endsWith("\u001a") ? source : source + "\u001a";
        for (int i = 0; i < source.length(); i++) {
            Character c = source.charAt(i);
            States state = stateMachine.process(source.charAt(i));
            System.out.println(String.format("%s %s", c, state));
        }
        System.out.println();
        words.forEach(System.out::println);
        Assert.assertEquals(new SourceWord(SourceWordType.INSTRUCTION, "aaa"), words.get(0));
        Assert.assertEquals(new SourceWord(SourceWordType.STRING, "ss\\\" z"), words.get(1));
        Assert.assertEquals(new SourceWord(SourceWordType.INTNUMBER, "123"), words.get(2));
        Assert.assertEquals(new SourceWord(SourceWordType.FLOATNUMBER, "1.2"), words.get(3));
        Assert.assertEquals(new SourceWord(SourceWordType.INSTRUCTION, "ii:./uu"), words.get(4));
    }
}
