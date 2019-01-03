package com.vtxsystems.statemachine;

import org.junit.Test;

import java.util.*;

import static com.vtxsystems.statemachine.CharacterHelpers.*;
import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.SourceWordType.*;
import static com.vtxsystems.statemachine.SimpleStateMachineTextTest.States.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class SimpleStateMachineTextTest {
    enum States {
        INSIDE_WORD,
        INSIDE_COMMENT,
        INSIDE_SPACE,
        INSIDE_STRING,
        INSIDE_NUMBER,
        INSIDE_FLOAT,
        STRING_START,
        INSIDE_STRING_ESCAPED_CHAR,
        POSSIBLY_COMMENT,
        FINISHED
    }

    enum SourceWordType {
        INSTRUCTION,
        STRING,
        INTNUMBER,
        FLOATNUMBER
    }

    private static final char[] SPECIAL_CHAR_ARR = {
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
    private static final Set<Character> SPECIAL_CHARS = new HashSet<Character>() {{
        for (char c : SPECIAL_CHAR_ARR) {
            add(c);
        }
    }};

    private static final Set<Character> START_IDENTIFIER_CHARS = new HashSet<Character>() {{
        addAll(LOWER_LETTER_CHARS);
        addAll(UPPER_LETTER_CHARS);
    }};

    private static final Set<Character> WORD_CHARS = new HashSet<Character>() {{
        addAll(LOWER_LETTER_CHARS);
        addAll(UPPER_LETTER_CHARS);
        addAll(DIGIT_CHARS);
        addAll(SPECIAL_CHARS);
    }};

    StringBuilder accumulator = null;
    List<SourceWord> words = new ArrayList<>();

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

    private void init() {
        accumulator = new StringBuilder();
    }

    private void accumulate(States oldState, States state, Object e) {
        SourceWordType swt = null;
        switch (oldState) {
            case INSIDE_WORD: swt = INSTRUCTION; break;
            case INSIDE_NUMBER: swt = INTNUMBER; break;
            case INSIDE_FLOAT: swt = FLOATNUMBER; break;
            case INSIDE_STRING: swt = STRING; break;
        }
        words.add(new SourceWord(swt, accumulator.toString()));
        accumulator = new StringBuilder();
    }

    @Test
    public void textSimpleTest() {
        StateMachine<States, Character> stateMachine = new SimpleStateMachineBuilder<States, Character>()
                .addOnBeginWorkCallback(e -> init())

                .addState(INSIDE_SPACE)
                .addState(INSIDE_COMMENT)
                .addState(INSIDE_WORD, e -> accumulator.append(e))
                .addState(INSIDE_STRING, e -> accumulator.append(e))
                .addState(INSIDE_NUMBER, e -> accumulator.append(e))
                .addState(INSIDE_FLOAT, e -> accumulator.append(e))
                .addState(POSSIBLY_COMMENT)
                .addState(STRING_START)
                .addState(INSIDE_STRING_ESCAPED_CHAR)

                .addFinalStates(FINISHED)

                .addTransition(INSIDE_SPACE, '"', STRING_START)
                .addTransition(INSIDE_SPACE, START_IDENTIFIER_CHARS, INSIDE_WORD)
                .addTransition(INSIDE_SPACE, DIGIT_CHARS, INSIDE_NUMBER)
                .addTransition(INSIDE_SPACE, '/', POSSIBLY_COMMENT)
                .addTransition(INSIDE_SPACE, SPACE_CHARS, INSIDE_SPACE)
                .addTransition(INSIDE_SPACE, '\u001a', FINISHED)

                .addTransition(POSSIBLY_COMMENT, '/', INSIDE_COMMENT)

                .addTransition(STRING_START, WORD_CHARS, INSIDE_STRING)
                .addTransition(STRING_START, SPACE_CHARS, INSIDE_STRING)
                .addTransition(STRING_START, '\\', INSIDE_STRING_ESCAPED_CHAR)
                .addTransition(STRING_START, '.', INSIDE_STRING)
                .addTransition(STRING_START, '"', new Transition<>(INSIDE_SPACE, (o, s, e) -> accumulate(o, s, e)))

                .addTransition(INSIDE_NUMBER, '.', INSIDE_FLOAT)
                .addTransition(INSIDE_NUMBER, DIGIT_CHARS, INSIDE_NUMBER)
                .addTransition(INSIDE_NUMBER, SPACE_CHARS, new Transition<>(INSIDE_SPACE, (o, s, e) -> accumulate(o, s, e)))

                .addTransition(INSIDE_FLOAT, SPACE_CHARS, new Transition<>(INSIDE_SPACE, (o, s, e) -> accumulate(o, s, e)))
                .addTransition(INSIDE_FLOAT, DIGIT_CHARS, INSIDE_FLOAT)

                .addTransition(INSIDE_COMMENT, WORD_CHARS, INSIDE_COMMENT)
                .addTransition(INSIDE_COMMENT, SPACE_CHARS, INSIDE_COMMENT)
                .addTransition(INSIDE_COMMENT, '\n', INSIDE_SPACE)

                .addTransition(INSIDE_WORD, WORD_CHARS, INSIDE_WORD)
                .addTransition(INSIDE_WORD, SPECIAL_CHARS, INSIDE_WORD)
                .addTransition(INSIDE_WORD, '.', INSIDE_WORD)
                .addTransition(INSIDE_WORD, SPACE_CHARS, new Transition<>(INSIDE_SPACE, (o, s, e) -> accumulate(o, s, e)))

                .addTransition(INSIDE_STRING, WORD_CHARS, INSIDE_STRING)
                .addTransition(INSIDE_STRING, SPACE_CHARS, INSIDE_STRING)
                .addTransition(INSIDE_STRING, '\\', INSIDE_STRING_ESCAPED_CHAR)
                .addTransition(INSIDE_STRING, '.', INSIDE_STRING)
                .addTransition(INSIDE_STRING, '"', new Transition<>(INSIDE_SPACE, (o, s, e) -> accumulate(o, s, e)))

                .addTransition(INSIDE_STRING_ESCAPED_CHAR, '"', INSIDE_STRING)
                .addTransition(INSIDE_STRING_ESCAPED_CHAR, '\\', new Transition<>(INSIDE_STRING, (o, s, e) -> accumulator.append(e)))

                .addOnUnexpectedEventCallback(ex -> {
                            List<Character> cl = new ArrayList<>();
                            ex.getEventHistory().forEach(o -> cl.add((Character) o));
                            System.out.println(charListToString(cl));
                        }
                )

                .setInitialState(INSIDE_SPACE)

                .build();

        String source = "aaa \"ss\\\" z\"  123 1.2 // cc \nii:./uu ";
        source = source.endsWith("\u001a") ? source : source + "\u001a";
        stateMachine.processEvents(source.chars().mapToObj(e -> (char) e).collect(toList()));
        assertEquals(new SourceWord(INSTRUCTION, "aaa"), words.get(0));
        assertEquals(new SourceWord(SourceWordType.STRING, "ss\" z"), words.get(1));
        assertEquals(new SourceWord(SourceWordType.INTNUMBER, "123"), words.get(2));
        assertEquals(new SourceWord(SourceWordType.FLOATNUMBER, "1.2"), words.get(3));
        assertEquals(new SourceWord(INSTRUCTION, "ii:./uu"), words.get(4));
    }
}
