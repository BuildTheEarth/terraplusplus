package net.buildtheearth.terraplusplus.util.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.daporkchop.lib.common.util.PArrays;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

import static net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException.*;

/**
 * Utilities for assisting in the parsing of latitude and longitude strings into Decimals.
 * <p>
 * Adapted version from gbif parsers
 */
public final class CoordinateParseUtils {

    private final static String DMS = "\\s*(\\d{1,3})\\s*(?:°|d|º| |g|o)"  // The degrees
                                      + "\\s*([0-6]?\\d)\\s*(?:'|m| |´|’|′)" // The minutes
                                      + "\\s*(?:"                            // Non-capturing group
                                      + "([0-6]?\\d(?:[,.]\\d+)?)"           // Seconds and optional decimal
                                      + "\\s*(?:\"|''|s|´´|″)?"
                                      + ")?\\s*";
    private final static String DM = "\\s*(\\d{1,3})\\s*(?:°|d|º| |g|o)" // The degrees
                                     + "\\s*(?:"                           // Non-capturing group
                                     + "([0-6]?\\d(?:[,.]\\d+)?)"          // Minutes and optional decimal
                                     + "\\s*(?:'|m| |´|’|′)?"
                                     + ")?\\s*";
    private final static String D = "\\s*(\\d{1,3}(?:[,.]\\d+)?)\\s*(?:°|d|º| |g|o|)\\s*"; // The degrees and optional decimal
    private final static Pattern DMS_SINGLE = Pattern.compile("^" + DMS + "$", Pattern.CASE_INSENSITIVE);
    private final static Pattern DM_SINGLE = Pattern.compile("^" + DM + "$", Pattern.CASE_INSENSITIVE);
    private final static Pattern D_SINGLE = Pattern.compile("^" + D + "$", Pattern.CASE_INSENSITIVE);
    private final static Pattern DMS_COORD = Pattern.compile("^" + DMS + "([NSEOW])" + "[ ,;/]?" + DMS + "([NSEOW])$", Pattern.CASE_INSENSITIVE);
    private final static String POSITIVE = "NEO";

    private static final char[] PART_DELIMITERS = ";/,".toCharArray();
    private static final List<Function<String, Double>> NUMBER_PARSERS;
    static {
        List<Function<String, Double>> parsers = new ArrayList<>();
        parsers.add(Double::parseDouble);
        parsers.add(CoordinateParseUtils::parseDoubleWithCommaDecimalSeparator);
        NUMBER_PARSERS = Collections.unmodifiableList(parsers);
    }

    private static boolean inRange(double lat, double lon) {
        return Double.compare(lat, 90) <= 0 && Double.compare(lat, -90) >= 0 && Double.compare(lon, 180) <= 0 && Double.compare(lon, -180) >= 0;
    }

    private static boolean isLat(String direction) {
        return "NS".contains(direction.toUpperCase());
    }

    private static int coordSign(String direction) {
        return POSITIVE.contains(direction.toUpperCase()) ? 1 : -1;
    }

    /**
     * Parses a String to a {@link LatLng} object, while being as flexible as possible.
     * It can understand both decimal and DMS coordinates, and even a mix of them.
     * <p>
     * Coordinates can use both a period <code>.</code>, or a comma <code>,</code> as the decimal separator,
     * as long as the same is used for both the latitude and longitude.
     * <p>
     * The coordinates can be separated by whitespaces, a semicolon <code>;</code>, a slash <code>/</code> or a comma <code>,</code>.
     * <p>
     * If the context doesn't make it clear which of latitude or longitude comes first, latitude is assumed.
     *
     * @param coordinates   the coordinates to parse
     *
     * @return the parsed coordinates
     */
    public static LatLng parseVerbatimCoordinates(final String coordinates) {
        if (Strings.isNullOrEmpty(coordinates)) {
            return null;
        }
        try {

            // Try parsing a string without a clear separator
            Matcher m = DMS_COORD.matcher(coordinates);
            if (m.find()) {

                final String direction1 = m.group(4);
                final String direction2 = m.group(8);

                for (Function<String, Double> format: NUMBER_PARSERS) {

                    try {

                        // First parse coordinates regardless whether they are lat or lon
                        double coordinate1 = coordFromMatcher(m, 1, 2, 3, direction1, format);
                        double coordinate2 = coordFromMatcher(m, 5, 6, 7, direction2, format);

                        // Now see what order the coordinates are in:
                        if (isLat(direction1) && !isLat(direction2)) {
                            checkLongitudeLatitudeInRange(coordinate2, coordinate1);
                            return validateAndRound(coordinate1, coordinate2);
                        } else if (!isLat(direction1) && isLat(direction2)) {
                            return validateAndRound(coordinate2, coordinate1);
                        } else {
                            return null;
                        }

                    } catch (Exception ignored) {
                    }

                }

                return null;

            }

            // Now try parsing with a separator
            String[] latlon = separateParts(coordinates);

            // Try with DMS format
            for (Function<String, Double> format: NUMBER_PARSERS) {
                try {
                    double lat = parseDMS(latlon[0], true, format);
                    double lon = parseDMS(latlon[1], false, format);
                    return validateAndRound(lat, lon);
                } catch (IllegalArgumentException | OutOfProjectionBoundsException ignored) {
                }
            }

            // Try with literal values
            for (Function<String, Double> format: NUMBER_PARSERS) {
                try {
                    double lat = format.apply(latlon[0]);
                    double lon = format.apply(latlon[1]);
                    return validateAndRound(lat, lon);
                } catch (Exception ignored) {
                }
            }

            // Failed :(
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LatLng validateAndRound(double lat, double lon) throws OutOfProjectionBoundsException {
        lat = roundTo6decimals(lat);
        lon = roundTo6decimals(lon);

        if (Double.compare(lat, 0) == 0 && Double.compare(lon, 0) == 0) {
            return new LatLng(0, 0);
        }

        if (inRange(lat, lon)) {
            return new LatLng(lat, lon);
        }

        if (Double.compare(lat, 90) > 0 || Double.compare(lat, -90) < 0) {
            // try and swap
            if (inRange(lon, lat)) {
                return new LatLng(lat, lon);
            }
        }

        throw OutOfProjectionBoundsException.get();
    }

    private static String[] separateParts(String coordinates) throws NumberFormatException {

        // First, remove leading and trailing blanks
        coordinates = coordinates.trim();

        // Then, look for the first blank, if there is one
        int firstBlankStart = coordinates.indexOf(" ");
        if (firstBlankStart > 0) {
            int firstBlankEnd = firstBlankStart;
            while (coordinates.charAt(firstBlankEnd) == ' ') firstBlankEnd++;

            // Then look for any other blank, if there is one, the string is malformed
            if (coordinates.indexOf(' ', firstBlankEnd) >= 0) throw new NumberFormatException();

            // If the first char directly before the blank is a part separator,
            // and is the only occurrence, consider it part of the blank
            char charBeforeBlank = coordinates.charAt(firstBlankStart - 1);
            if (firstBlankStart > 1
                    && PArrays.linearSearch(PART_DELIMITERS, charBeforeBlank) >= 0
                    && coordinates.indexOf(charBeforeBlank, firstBlankEnd) < 0) {
                firstBlankStart--;
            }

            // Split at the blank
            return new String[] {coordinates.substring(0, firstBlankStart), coordinates.substring(firstBlankEnd)};
        }

        // Look for a potential delimiter that only appears once and is not at the start or end of the string
        for (final char delimiter : PART_DELIMITERS) {
            int count = StringUtils.countMatches(coordinates, String.valueOf(delimiter));
            int delimiterIndex;
            if (count == 1 && (delimiterIndex = coordinates.indexOf(delimiter)) > 0 && delimiterIndex < coordinates.length() - 1) {
                return StringUtils.split(coordinates, delimiter);
            }
        }

        // We didn't manage to split, the string is malformed
        throw new NumberFormatException();

    }

    static double parseDMS(String coord, boolean lat, Function<String, Double> format) {
        final String DIRS = lat ? "NS" : "EOW";
        coord = coord.trim().toUpperCase();

        if (coord.length() > 3) {
            // preparse the direction and remove it from the string to avoid a very complex regex
            char dir = 'n';
            if (DIRS.contains(String.valueOf(coord.charAt(0)))) {
                dir = coord.charAt(0);
                coord = coord.substring(1);
            } else if (DIRS.contains(String.valueOf(coord.charAt(coord.length() - 1)))) {
                dir = coord.charAt(coord.length() - 1);
                coord = coord.substring(0, coord.length() - 1);
            }
            // without the direction chuck it at the regex
            Matcher m = DMS_SINGLE.matcher(coord);
            if (m.find()) {
                return coordFromMatcher(m, 1, 2, 3, String.valueOf(dir), format);
            } else {
                m = DM_SINGLE.matcher(coord);
                if (m.find()) {
                    return coordFromMatcher(m, 1, 2, String.valueOf(dir), format);
                } else {
                    m = D_SINGLE.matcher(coord);
                    if (m.find()) {
                        return coordFromMatcher(m, 1, String.valueOf(dir), format);
                    }
                }
            }
        }
        throw new IllegalArgumentException();
    }

    private static double coordFromMatcher(Matcher m, int idx1, int idx2, int idx3, String sign, Function<String, Double> format) {
        double degrees = format.apply(m.group(idx1));
        double minutes = format.apply(m.group(idx2));
        double seconds = format.apply(m.group(idx3));
        return roundTo6decimals(coordSign(sign) * dmsToDecimal(degrees, minutes, seconds));
    }

    private static double coordFromMatcher(Matcher m, int idx1, int idx2, String sign, Function<String, Double> format) {
        double degrees = format.apply(m.group(idx1));
        double minutes = format.apply(m.group(idx2));
        return roundTo6decimals(coordSign(sign) * dmsToDecimal(degrees, minutes, 0.0));
    }

    private static double coordFromMatcher(Matcher m, int idx1, String sign, Function<String, Double> format) {
        double degrees = format.apply(m.group(idx1));
        return roundTo6decimals(coordSign(sign) * dmsToDecimal(degrees, 0.0, 0.0));
    }

    private static double dmsToDecimal(double degree, Double minutes, Double seconds) {
        minutes = minutes == null ? 0 : minutes;
        seconds = seconds == null ? 0 : seconds;
        return degree + (minutes / 60) + (seconds / 3600);
    }

    // round to 6 decimals (~1m precision) since no way we're getting anything legitimately more precise
    private static Double roundTo6decimals(Double x) {
        return x == null ? null : Math.round(x * Math.pow(10, 6)) / Math.pow(10, 6);
    }

    private static double parseDoubleWithCommaDecimalSeparator(String str) {
        if (str.indexOf(".") > 0) throw new NumberFormatException("Illegal char in comma number: " + ".");
        return Double.parseDouble(str.replaceAll(",", "."));
    }

    private CoordinateParseUtils() {
        throw new UnsupportedOperationException("Can't initialize class");
    }
}
