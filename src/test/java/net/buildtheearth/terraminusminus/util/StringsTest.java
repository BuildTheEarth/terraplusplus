package net.buildtheearth.terraminusminus.util;

import net.buildtheearth.terraminusminus.TerraMinusMinus;
import org.junit.jupiter.api.Test;

import static net.buildtheearth.terraminusminus.util.Strings.*;
import static org.junit.jupiter.api.Assertions.*;


public class StringsTest extends TerraMinusMinus {

    @Test
    public void canCountMatchesWithStringNeedle() {
        assertEquals(0, countMatches(null, "abc"));
        assertEquals(0, countMatches("", "abc"));
        assertEquals(0, countMatches("abba", null));
        assertEquals(0, countMatches("abba", ""));
        assertEquals(2, countMatches("abba", "a"));
        assertEquals(1, countMatches("abba", "ab"));
        assertEquals(0, countMatches("abba", "xxx"));
        assertEquals(1, countMatches("ababa", "aba"));
        assertEquals(4, countMatches("aaaaaaaaa", "aa"));
    }

    @Test
    public void canCountMatchesWithCharNeedle() {
        assertEquals(0, countMatches(null, 'a'));
        assertEquals(0, countMatches("", 'a'));
        assertEquals(0, countMatches("1", 'a'));
        assertEquals(1, countMatches("abc", 'a'));
        assertEquals(2, countMatches("aba", 'a'));
        assertEquals(2, countMatches("aab", 'a'));
    }

    @Test
    public void canSplitStrings() {
        assertArrayEquals(new String[] {}, split(null, 'a'));
        assertArrayEquals(new String[] {""}, split("", 'b'));
        assertArrayEquals(new String[] {"", ""}, split("b", 'b'));
        assertArrayEquals(new String[] {"a", "a", "c", "def"}, split("ababcbdef", 'b'));
        assertArrayEquals(new String[] {"a", "a", "c", "def", ""}, split("ababcbdefb", 'b'));
        assertArrayEquals(new String[] {"", "a", "a", "c", "def"}, split("bababcbdef", 'b'));
    }

}
