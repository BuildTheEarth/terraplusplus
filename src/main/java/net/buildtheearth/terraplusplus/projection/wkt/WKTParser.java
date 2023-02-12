package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class WKTParser {
    public static WKTEllipsoid parseEllipsoid(@NonNull PushbackReader reader) throws IOException {
        return parseEllipsoid(reader, readKeyword(reader));
    }

    private static WKTEllipsoid parseEllipsoid(@NonNull PushbackReader reader, @NonNull String keyword) throws IOException {
        checkState("ELLIPSOID".equals(keyword) || "SPHEROID".equals(keyword), keyword);

        skipWhitespace(reader);
        readAndExpectChar(reader, '[');

        WKTEllipsoid.WKTEllipsoidBuilder builder = WKTEllipsoid.builder();
        builder.name(readQuotedLatinString(reader));

        skipWhitespace(reader);
        readAndExpectChar(reader, ',');
        builder.semiMajorAxis(readUnsignedNumericLiteral(reader));

        skipWhitespace(reader);
        readAndExpectChar(reader, ',');
        builder.inverseFlattening(readUnsignedNumericLiteral(reader));

        skipWhitespace(reader);
        char c = readChar(reader);
        switch (c) {
            case ',':
                String attributeKeyword = readKeyword(reader);
                switch (attributeKeyword) {
                    case "LENGTHUNIT":
                    case "UNIT":
                        builder.lengthUnit(parseLengthUnit(reader, attributeKeyword));
                        break;
                    default:
                        throw new IllegalArgumentException(attributeKeyword);
                }
                break;
            case ']':
                return builder.build();
            default:
                throw new IllegalArgumentException(String.valueOf(c));
        }

        parseRemainingAttributes(reader, builder);
        return builder.build();
    }

    public static WKTLengthUnit parseLengthUnit(@NonNull PushbackReader reader) throws IOException {
        return parseLengthUnit(reader, readKeyword(reader));
    }

    private static WKTLengthUnit parseLengthUnit(@NonNull PushbackReader reader, @NonNull String keyword) throws IOException {
        checkState("LENGTHUNIT".equals(keyword) || "UNIT".equals(keyword), keyword);

        skipWhitespace(reader);
        readAndExpectChar(reader, '[');

        WKTLengthUnit.WKTLengthUnitBuilder builder = WKTLengthUnit.builder();
        builder.name(readQuotedLatinString(reader));

        skipWhitespace(reader);
        readAndExpectChar(reader, ',');
        builder.conversionFactor(readUnsignedNumericLiteral(reader));

        parseRemainingAttributes(reader, builder);

        return builder.build();
    }

    private static void parseRemainingAttributes(@NonNull PushbackReader reader, @NonNull WKTObject.WKTObjectBuilder<?, ?> builder) throws IOException {
        while (true) {
            skipWhitespace(reader);
            char c = readChar(reader);
            switch (c) {
                case ',':
                    String attributeKeyword = readKeyword(reader);
                    switch (attributeKeyword) {
                        case "ID":
                        case "AUTHORITY":
                            builder.id(parseAuthority(reader, attributeKeyword));
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

    private static WKTID parseAuthority(@NonNull PushbackReader reader) throws IOException {
        return parseAuthority(reader, readKeyword(reader));
    }

    private static WKTID parseAuthority(@NonNull PushbackReader reader, @NonNull String keyword) throws IOException {
        checkState("ID".equals(keyword) || "AUTHORITY".equals(keyword), keyword);

        skipWhitespace(reader);
        readAndExpectChar(reader, '[');

        WKTID.WKTIDBuilder builder = WKTID.builder();
        builder.authorityName(readQuotedLatinString(reader));

        skipWhitespace(reader);
        readAndExpectChar(reader, ',');
        skipWhitespace(reader);

        char c = readChar(reader);
        reader.unread(c);
        if (c == '"') {
            builder.authorityUniqueIdentifier(readQuotedLatinString(reader));
        } else {
            builder.authorityUniqueIdentifier(readUnsignedInteger(reader));
        }

        skipWhitespace(reader);
        readAndExpectChar(reader, ']');

        return builder.build();
    }

    private static String readKeyword(@NonNull PushbackReader reader) throws IOException {
        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder builder = handle.get();
            builder.setLength(0);

            skipWhitespace(reader);

            //read until we find a character that isn't an uppercase latin char
            while (true) {
                char c = readChar(reader);
                if (c >= 'A' && c <= 'Z') {
                    builder.append(c);
                } else {
                    reader.unread(c);
                    break;
                }
            }

            return builder.toString().intern();
        }
    }

    /**
     * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#15>WKT Specification §6.3.4: WKT characters</a>
     */
    private static String readQuotedLatinString(@NonNull PushbackReader reader) throws IOException {
        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder builder = handle.get();
            builder.setLength(0);

            //find the opening quote
            skipWhitespace(reader);
            readAndExpectChar(reader, '"');

            //read until we find a closing quote
            while (true) {
                char c = readChar(reader);
                if (c == '"') { //this could be a closing quote
                    char next = readChar(reader);
                    if (next == '"') { //double doublequote -> doublequote
                        builder.append('"');
                    } else { //end of file
                        reader.unread(next);
                        break;
                    }
                } else {
                    builder.append(c);
                }
            }

            return builder.toString().intern();
        }
    }

    private static long readUnsignedInteger(@NonNull PushbackReader reader) throws IOException {
        skipWhitespace(reader);

        char c = readChar(reader);
        checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);

        long value = 0L;
        do {
            value = addExact(multiplyExact(value, 10L), c - '0');
            c = readChar(reader);
        } while (c >= '0' && c <= '9');
        reader.unread(c);

        return value;
    }

    private static long readSignedInteger(@NonNull PushbackReader reader) throws IOException {
        skipWhitespace(reader);

        char c = readChar(reader);
        switch (c) {
            case '-':
                return -readUnsignedInteger(reader);
            default:
                reader.unread(c);
            case '+':
                return readUnsignedInteger(reader);
        }
    }

    private static double readUnsignedNumericLiteral(@NonNull PushbackReader reader) throws IOException {
        skipWhitespace(reader);

        char c = readChar(reader);
        if (c == '.') { //<exact numeric literal>: second case
            return readExactNumericLiteral_fractionalPart(reader);
        } else {
            reader.unread(c);
        }

        double value = readUnsignedInteger(reader);

        c = readChar(reader);
        switch (c) {
            case 'E': //<approximate numeric literal>
                return value * pow(10.0d, readSignedInteger(reader));
            case '.': //<exact numeric literal>: first case
                c = readChar(reader);
                reader.unread(c);
                if (c >= '0' && c <= '9') {
                    value += readExactNumericLiteral_fractionalPart(reader);

                    c = readChar(reader);
                    if (c == 'E') { //<approximate numeric literal>
                        return value * pow(10.0d, readSignedInteger(reader));
                    } else {
                        reader.unread(c);
                    }
                } else if (c == 'E') { //<approximate numeric literal>
                    readChar(reader);
                    return value * pow(10.0d, readSignedInteger(reader));
                }
                return value;
            default:
                reader.unread(c);
                return value;
        }
    }

    private static double readExactNumericLiteral_fractionalPart(@NonNull PushbackReader reader) throws IOException {
        double value = 0.0d;
        double factor = 0.1d;

        char c = readChar(reader);
        checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);
        do {
            value += factor * (c - '0');
            factor *= 0.1d;

            c = readChar(reader);
        } while (c >= '0' && c <= '9');
        reader.unread(c);

        return value;
    }

    private static void skipWhitespace(@NonNull PushbackReader reader) throws IOException {
        while (true) {
            char c = readChar(reader);
            if (!isWhitespace(c)) {
                reader.unread(c);
                return;
            }
        }
    }

    private static void readAndExpectChar(@NonNull PushbackReader reader, char expected) throws IOException {
        char c = readChar(reader);
        checkState(c == expected, "expected '%c', but found '%c'", expected, c);
    }

    private static char readChar(@NonNull PushbackReader reader) throws IOException {
        int c = reader.read();
        if (c < 0) {
            throw new EOFException();
        }
        return (char) c;
    }

    private static boolean isWhitespace(char c) {
        return c <= ' ';
    }
}
