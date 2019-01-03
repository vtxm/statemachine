package com.vtxsystems.statemachine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterHelpers {
    private static final char[] SPACE_CHAR_ARR = { ' ', '\n', '\t', '\u001a' };
    public static final Set<Character> SPACE_CHARS = new HashSet<Character>() {{
        for (char c : SPACE_CHAR_ARR) {
            add(c);
        }
    }};

    private static final char[] LOWER_LETTER_CHAR_ARR = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    public static final Set<Character> LOWER_LETTER_CHARS = new HashSet<Character>() {{
        for (char c : LOWER_LETTER_CHAR_ARR) {
            add(c);
        }
    }};

    private static final char[] UPPER_LETTER_CHAR_ARR = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    public static final Set<Character> UPPER_LETTER_CHARS = new HashSet<Character>() {{
        for (char c : UPPER_LETTER_CHAR_ARR) {
            add(c);
        }
    }};

    private static final char[] DIGIT_CHAR_ARR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final Set<Character> DIGIT_CHARS = new HashSet<Character>() {{
        for (char c : DIGIT_CHAR_ARR) {
            add(c);
        }
    }};

    public static String charListToString(List<Character> chars) {
        StringBuilder sb = new StringBuilder();
        for (Character c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
}
