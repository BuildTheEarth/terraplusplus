package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.regex.Pattern;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class WKTParser {
    // https://docs.opengeospatial.org/is/18-010r7/18-010r7.html#13
    private static final Pattern period = Pattern.compile("\\.");
    private static final Pattern sign = Pattern.compile("[+\\-]");
    private static final Pattern digit = Pattern.compile("\\d");
    private static final Pattern unsigned_integer = Pattern.compile(digit + "+");
    private static final Pattern signed_integer = Pattern.compile("(?:" + sign + ")?" + unsigned_integer);
    private static final Pattern exponent = signed_integer;
    private static final Pattern exact_numeric_literal = Pattern.compile(unsigned_integer + "(?:" + period + "(?:" + unsigned_integer + ")?)?|" + period + unsigned_integer);
    private static final Pattern mantissa = exact_numeric_literal;
    private static final Pattern approximate_numeric_literal = Pattern.compile("(?:" + mantissa + ")E" + exponent);
    private static final Pattern unsigned_numeric_literal = Pattern.compile("(?>" + approximate_numeric_literal + ")|" + exact_numeric_literal);
    private static final Pattern signed_numeric_literal = Pattern.compile("(?:" + sign + ")?(?:" + unsigned_numeric_literal + ')');
    private static final Pattern number = Pattern.compile(signed_numeric_literal + "|" + unsigned_numeric_literal);

    @SneakyThrows(IOException.class)
    public static WKTEllipsoid parseEllipsoid(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTEllipsoid.PARSE_SCHEMA.parse(reader);
        }

        //return parseEllipsoid(buffer, readKeyword(buffer));
    }

    public static WKTLengthUnit parseLengthUnit(@NonNull CharBuffer buffer) {
        return parseLengthUnit(buffer, readKeyword(buffer));
    }

    private static WKTLengthUnit parseLengthUnit(@NonNull CharBuffer buffer, @NonNull String keyword) {
        checkState("LENGTHUNIT".equals(keyword) || "UNIT".equals(keyword), keyword);

        skipWhitespace(buffer);
        readAndExpectChar(buffer, '[');

        WKTLengthUnit.WKTLengthUnitBuilder builder = WKTLengthUnit.builder();
        builder.name(readQuotedLatinString(buffer));

        skipWhitespace(buffer);
        readAndExpectChar(buffer, ',');
        builder.conversionFactor(readUnsignedNumericLiteral(buffer));

        parseRemainingAttributes(buffer, builder);

        return builder.build();
    }

    private static void parseRemainingAttributes(@NonNull CharBuffer buffer, @NonNull WKTObject.WKTObjectBuilder<?, ?> builder) {
        while (true) {
            skipWhitespace(buffer);
            char c = buffer.get();
            switch (c) {
                case ',':
                    String attributeKeyword = readKeyword(buffer);
                    switch (attributeKeyword) {
                        case "ID":
                        case "AUTHORITY":
                            builder.id(parseAuthority(buffer, attributeKeyword));
                            break;
                        default:
                            throw new IllegalArgumentException(attributeKeyword);
                    }
                    break;
                case ']':
                    return;
                default:
                    throw new IllegalArgumentException(String.valueOf(c));
            }
        }
    }

    private static WKTID parseAuthority(@NonNull CharBuffer buffer) {
        return parseAuthority(buffer, readKeyword(buffer));
    }

    private static WKTID parseAuthority(@NonNull CharBuffer buffer, @NonNull String keyword) {
        checkState("ID".equals(keyword) || "AUTHORITY".equals(keyword), keyword);

        skipWhitespace(buffer);
        readAndExpectChar(buffer, '[');

        WKTID.WKTIDBuilder builder = WKTID.builder();
        builder.authorityName(readQuotedLatinString(buffer));

        skipWhitespace(buffer);
        readAndExpectChar(buffer, ',');
        skipWhitespace(buffer);

        char c = buffer.charAt(0);
        if (c == '"') {
            builder.authorityUniqueIdentifier(readQuotedLatinString(buffer));
        } else {
            builder.authorityUniqueIdentifier(readUnsignedInteger(buffer));
        }

        skipWhitespace(buffer);
        readAndExpectChar(buffer, ']');

        return builder.build();
    }

    private static String readKeyword(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        int start = buffer.position();
        int end = start;
        for (char c; (c = buffer.get(end)) >= 'A' && c <= 'Z'; ) { //find the index of the first non-alphabetic char
            end++;
        }
        String value = buffer.subSequence(0, end - start).toString();
        buffer.position(end);
        return value;
    }

    /**
     * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#15>WKT Specification §6.3.4: WKT characters</a>
     */
    private static String readQuotedLatinString(@NonNull CharBuffer buffer) {
        //find the opening quote
        skipWhitespace(buffer);
        readAndExpectChar(buffer, '"');

        //read until we find a closing quote
        int start = buffer.position();
        int end = start;
        for (char c; ; ) {
            c = buffer.get(end);
            if (c == '"') { //this could be a closing quote
                //peek at the next char to see if this is a double doublequote
                if (buffer.get(end + 1) == '"') {
                    end += 2; //double doublequote -> doublequote, skip it and advance
                } else {
                    break;
                }
            } else {
                end++;
            }
        }

        String value = buffer.subSequence(0, end - start).toString().replace("\"\"", "\"");
        buffer.position(end);
        readAndExpectChar(buffer, '"');
        return value;
    }

    /*private static long readSignedInteger(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        Matcher matcher = signed_integer.matcher(buffer);
        checkState(matcher.find(), "expected <signed_integer> at %d", buffer.position());

        long value = Long.parseLong(matcher.group());
        buffer.position(buffer.position() + matcher.end());
        return value;
    }

    private static long readUnsignedInteger(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        Matcher matcher = unsigned_integer.matcher(buffer);
        checkState(matcher.find(), "expected <unsigned_integer> at %d", buffer.position());

        long value = Long.parseUnsignedLong(matcher.group());
        buffer.position(buffer.position() + matcher.end());
        return value;
    }

    private static double readUnsignedNumericLiteral(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        Matcher matcher = unsigned_numeric_literal.matcher(buffer);
        checkState(matcher.find(), "expected <unsigned_numeric_literal> at %d", buffer.position());

        double value = Double.parseDouble(matcher.group());
        buffer.position(buffer.position() + matcher.end());
        return value;
    }*/

    private static long readUnsignedInteger(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        char c = buffer.get();
        checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);

        long value = 0L;
        do {
            value = addExact(multiplyExact(value, 10L), c - '0');
            c = buffer.get();
        } while (c >= '0' && c <= '9');
        buffer.position(buffer.position() - 1);

        return value;
    }

    private static long readSignedInteger(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        char c = buffer.get();
        switch (c) {
            case '-':
                return -readUnsignedInteger(buffer);
            default:
                buffer.position(buffer.position() - 1);
            case '+':
                return readUnsignedInteger(buffer);
        }
    }

    private static double readUnsignedNumericLiteral(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);

        char c = buffer.get();
        if (c == '.') { //<exact numeric literal>: second case
            return readExactNumericLiteral_fractionalPart(buffer);
        } else {
            buffer.position(buffer.position() - 1);
        }

        double value = readUnsignedInteger(buffer);

        c = buffer.get();
        switch (c) {
            case 'E': //<approximate numeric literal>
                return value * pow(10.0d, readSignedInteger(buffer));
            case '.': //<exact numeric literal>: first case
                c = buffer.get();
                buffer.position(buffer.position() - 1);
                if (c >= '0' && c <= '9') {
                    value += readExactNumericLiteral_fractionalPart(buffer);

                    c = buffer.get();
                    if (c == 'E') { //<approximate numeric literal>
                        return value * pow(10.0d, readSignedInteger(buffer));
                    } else {
                        buffer.position(buffer.position() - 1);
                    }
                } else if (c == 'E') { //<approximate numeric literal>
                    buffer.get();
                    return value * pow(10.0d, readSignedInteger(buffer));
                }
                return value;
            default:
                buffer.position(buffer.position() - 1);
                return value;
        }
    }

    private static double readExactNumericLiteral_fractionalPart(@NonNull CharBuffer buffer) {
        double value = 0.0d;
        double factor = 0.1d;

        char c = buffer.get();
        checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);
        do {
            value += factor * (c - '0');
            factor *= 0.1d;

            c = buffer.get();
        } while (c >= '0' && c <= '9');
        buffer.position(buffer.position() - 1);

        return value;
    }
    
    /*private static double readNumber(@NonNull CharBuffer buffer) {
        skipWhitespace(buffer);
        
        Matcher matcher = number.matcher(buffer);
        checkState(matcher.find(), "expected <number> at %d", buffer.position());

        double value = Double.parseDouble(matcher.group());
        buffer.position(buffer.position() + matcher.end());
        return value;
    }*/

    private static void skipWhitespace(@NonNull CharBuffer buffer) {
        while (isWhitespace(buffer.get())) {
            //empty
        }

        //jump back to the previous position, as we've already read the first non-whitespace char
        buffer.position(buffer.position() - 1);
    }

    private static void readAndExpectChar(@NonNull CharBuffer buffer, char expected) {
        char c = buffer.get();
        checkState(c == expected, "expected '%c', but found '%c'", expected, c);
    }

    private static boolean isWhitespace(char c) {
        return c <= ' ';
    }
}
