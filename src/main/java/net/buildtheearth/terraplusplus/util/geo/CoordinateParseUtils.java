package net.buildtheearth.terraplusplus.util.geo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for assisting in the parsing of latitude and longitude strings into Decimals.
 * <p>
 * Adapted version from gbif parsers
 */
public class CoordinateParseUtils {
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

    private static boolean inRange(double lat, double lon) {
        return Double.compare(lat, 90) <= 0 && Double.compare(lat, -90) >= 0 && Double.compare(lon, 180) <= 0 && Double.compare(lon, -180) >= 0;
    }

    private static boolean isLat(String direction) {
        return "NS".contains(direction.toUpperCase());
    }

    private static int coordSign(String direction) {
        return POSITIVE.contains(direction.toUpperCase()) ? 1 : -1;
    }

    // 02° 49' 52" N	131° 47' 03" E
    public static EllipsoidalCoordinates parseVerbatimCoordinates(final String coordinates) {
        if (Strings.isNullOrEmpty(coordinates)) {
            return null;
        }
        Matcher m = DMS_COORD.matcher(coordinates);
        if (m.find()) {
            final String dir1 = m.group(4);
            final String dir2 = m.group(8);
            // first parse coords regardless whether they are lat or lon
            double c1 = coordFromMatcher(m, 1, 2, 3, dir1);
            double c2 = coordFromMatcher(m, 5, 6, 7, dir2);
            // now see what order the coords are in:
            if (isLat(dir1) && !isLat(dir2)) {
                return validateAndRound(c1, c2);

            } else if (!isLat(dir1) && isLat(dir2)) {
                return validateAndRound(c2, c1);

            } else {
                return null;
            }

        } else if (coordinates.length() > 4) {
            // try to split and then use lat/lon parsing
            for (final char delim : ",;/ ".toCharArray()) {
                int cnt = StringUtils.countMatches(coordinates, String.valueOf(delim));
                if (cnt == 1) {
                    String[] latlon = StringUtils.split(coordinates, delim);
                    if (latlon.length == 2) {

                        Double lat = null;
                        Double lon = null;
                        try {
                            lat = Double.parseDouble(latlon[0]);
                            lon = Double.parseDouble(latlon[1]);
                        } catch (Exception ignored) {
                        }

                        if (lat == null || lon == null) {

                            try {
                                lat = parseDMS(latlon[0], true);
                                lon = parseDMS(latlon[1], false);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        }

                        return validateAndRound(lat, lon);
                    }
                }
            }
        }
        return null;
    }

    private static EllipsoidalCoordinates validateAndRound(double lat, double lon) {
        lat = roundTo6decimals(lat);
        lon = roundTo6decimals(lon);

        if (Double.compare(lat, 0) == 0 && Double.compare(lon, 0) == 0) {
            return EllipsoidalCoordinates.zero();
        }

        if (inRange(lat, lon)) {
            return EllipsoidalCoordinates.fromLatLonDegrees(lat, lon);
        }

        if (Double.compare(lat, 90) > 0 || Double.compare(lat, -90) < 0) {
            // try and swap
            if (inRange(lon, lat)) {
                //TODO: i'm pretty sure this is wrong, and that the arguments here are in fact supposed to be reversed
                return EllipsoidalCoordinates.fromLatLonDegrees(lat, lon);
            }
        }

        return null;
    }

    /**
     * Parses a single DMS coordinate
     *
     * @param coord
     * @param lat
     * @return the converted decimal up to 6 decimals accuracy
     */
    @VisibleForTesting
    protected static double parseDMS(String coord, boolean lat) {
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
                return coordFromMatcher(m, 1, 2, 3, String.valueOf(dir));
            } else {
                m = DM_SINGLE.matcher(coord);
                if (m.find()) {
                    return coordFromMatcher(m, 1, 2, String.valueOf(dir));
                } else {
                    m = D_SINGLE.matcher(coord);
                    if (m.find()) {
                        return coordFromMatcher(m, 1, String.valueOf(dir));
                    }
                }
            }
        }
        throw new IllegalArgumentException();
    }

    private static double coordFromMatcher(Matcher m, int idx1, int idx2, int idx3, String sign) {
        return roundTo6decimals(coordSign(sign) *
                                dmsToDecimal(Double.parseDouble(m.group(idx1)), Double.parseDouble(m.group(idx2)), Double.parseDouble(m.group(idx3))));
    }

    private static double coordFromMatcher(Matcher m, int idx1, int idx2, String sign) {
        return roundTo6decimals(coordSign(sign) *
                                dmsToDecimal(Double.parseDouble(m.group(idx1)), Double.parseDouble(m.group(idx2)), 0.0));
    }

    private static double coordFromMatcher(Matcher m, int idx1, String sign) {
        return roundTo6decimals(coordSign(sign) *
                                dmsToDecimal(Double.parseDouble(m.group(idx1)), 0.0, 0.0));
    }

    //TODO: get rid of all the use of Double in here, it's gross

    private static double dmsToDecimal(double degree, Double minutes, Double seconds) {
        minutes = minutes == null ? 0 : minutes;
        seconds = seconds == null ? 0 : seconds;
        return degree + (minutes / 60) + (seconds / 3600);
    }

    // round to 6 decimals (~1m precision) since no way we're getting anything legitimately more precise
    private static Double roundTo6decimals(Double x) {
        return x == null ? null : Math.round(x * 1e6) * 1e-6;
    }

    private CoordinateParseUtils() {
        throw new UnsupportedOperationException("Can't initialize class");
    }
}
