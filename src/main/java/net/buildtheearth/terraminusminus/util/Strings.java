package net.buildtheearth.terraminusminus.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

/**
 * String related utility functions.
 *
 * @author Smyler
 */
@UtilityClass
public class Strings {

    public String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Tests whether a {@link String string} is null or empty.
     *
     * @param string a string to test
     * @return true if the string is either null or the empty string, false otherwise
     */
    public boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Counts the number of times a substring appears in a string.
     * Occurrences of the substring overlapping each other count as one.
     *
     * @param haystack the string to search in
     * @param needle   the substring to search
     * @return the number of occurrences of the substring in the string, or 0 if either is null
     */
    public static int countMatches(@Nullable CharSequence haystack, @Nullable CharSequence needle) {
        if (haystack == null || needle == null) {
            return 0;
        }
        int matches = 0;
        for (int i = 0; i < haystack.length(); i++) {
            for (int j = 0; i < haystack.length() && j < needle.length(); j++) {
                if (haystack.charAt(i) != needle.charAt(j)) {
                    break;
                } else if (j == needle.length() - 1) {
                    matches++;
                } else {
                    i++;
                }
            }
        }
        return matches;
    }

    /**
     * Counts the number of times a chars appears in a string.
     *
     * @param haystack the string to search in
     * @param needle   the char to search
     * @return the number of occurrences of the substring in the string, or 0 if the string is null
     */
    public static int countMatches(@Nullable CharSequence haystack, char needle) {
        if (haystack == null) {
            return 0;
        }
        int matches = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Splits a string into a string array using a given delimiter.
     * The delimiter is not included in the members of the resulting array.
     * A null string returns an empty array.
     *
     * @param str       the string to split
     * @param delimiter the delimiter to split the string with
     * @return an array where each entry is a section of the origin string that was delimited by the given delimiter
     */
    public @NonNull String[] split(@Nullable CharSequence str, char delimiter) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        }
        String[] result = new String[countMatches(str, delimiter) + 1];
        int startIndex = 0;
        for (int resIndex = 0; resIndex < result.length; resIndex++) {
            for (int i = startIndex; i <= str.length(); i++) {
                if (i >= str.length() || str.charAt(i) == delimiter) {
                    result[resIndex] = str.subSequence(startIndex, i).toString();
                    startIndex = i + 1;
                    break;
                }
            }
        }
        return result;
    }

}
