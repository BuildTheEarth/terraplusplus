package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PorkUtil;

import java.io.IOException;
import java.nio.CharBuffer;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public interface WKTReader extends AutoCloseable {
    /**
     * Gets the type of the next token without consuming it.
     */
    Token peek() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#BEGIN_OBJECT} and returns the object's keyword.
     */
    String nextKeyword() throws IOException;

    /**
     * Reads the next token from the stream and asserts that it is {@link Token#END_OBJECT}.
     */
    void nextObjectEnd() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#QUOTED_LATIN_STRING} and returns the string value.
     */
    String nextQuotedLatinString() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#NUMBER} and returns the numeric value.
     */
    Number nextUnsignedNumericLiteral() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#NUMBER} and returns the numeric value.
     */
    Number nextSignedNumericLiteral() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#ENUM} and returns the enum value.
     */
    String nextEnumName() throws IOException;

    /**
     * Reads the next token from the stream, asserts that it is {@link Token#ENUM} and returns the enum value.
     */
    default <E extends Enum<E>> E nextEnum(@NonNull Class<E> enumClass) throws IOException {
        return Enum.valueOf(enumClass, this.nextEnumName());
    }

    @Override
    void close() throws IOException;

    /**
     * @author DaPorkchop_
     */
    enum Token {
        BEGIN_OBJECT,
        END_OBJECT,
        QUOTED_LATIN_STRING,
        NUMBER,
        ENUM,
    }

    /**
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    class FromCharBuffer implements WKTReader {
        @NonNull
        protected final CharBuffer buffer;

        protected boolean expectingFirstObjectProperty = true;

        protected static boolean isWhitespace(char c) {
            return c <= ' ';
        }

        protected void skipWhitespace() {
            int pos = this.buffer.position();
            while (isWhitespace(this.buffer.get(pos))) {
                pos++;
            }
            this.buffer.position(pos);
        }

        protected void skipToNextValue() {
            this.skipWhitespace();
            if (!this.expectingFirstObjectProperty) {
                char c = this.buffer.get();
                checkState(c == ',', "expected comma: '%c'", c);
                this.skipWhitespace();
            }
            this.expectingFirstObjectProperty = false;
        }

        @Override
        public Token peek() {
            for (int i = 0; ; i++) {
                char c = this.buffer.charAt(i);

                if (isWhitespace(c) || c == ',') {
                    continue;
                }

                if (c >= 'A' && c <= 'Z') {
                    //find the next character after the word token to determine whether this is a BEGIN_OBJECT or an ENUM
                    do {
                        c = this.buffer.charAt(++i);
                    } while ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_');

                    if (isWhitespace(c)) {
                        do { //skip any whitespace
                            i++;
                        } while (isWhitespace(c = this.buffer.charAt(i)));
                    }

                    switch (c) {
                        case ',':
                        case ']':
                            return Token.ENUM;
                        case '[':
                            return Token.BEGIN_OBJECT;
                    }
                    throw new IllegalStateException("unexpected character after keyword: '" + c + '"');
                } else if (c >= '0' && c <= '9') {
                    return Token.NUMBER;
                }

                switch (c) {
                    case '"':
                        return Token.QUOTED_LATIN_STRING;
                    case ']':
                        return Token.END_OBJECT;
                    case '+':
                    case '-':
                    case '.':
                        return Token.NUMBER;
                }
                throw new IllegalStateException("unexpected character: '" + c + '"');
            }
        }

        @Override
        public String nextKeyword() {
            this.skipToNextValue();

            int start = this.buffer.position();
            char c = this.buffer.get();
            checkState(c >= 'A' && c <= 'Z', "not a valid keyword start character: '%c'", c);

            while (!isWhitespace(c = this.buffer.get()) && c != '[') {
                checkState((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_', "not a valid keyword character: '%c'", c);
            }

            int end = this.buffer.position();
            if (c != '[') {
                this.skipWhitespace();
                checkState((c = this.buffer.get()) == '[', "expected begin object character, but found '%c'", c);
            }

            this.expectingFirstObjectProperty = true;
            return this.buffer.duplicate().position(start).limit(end - 1).toString();
        }

        @Override
        public void nextObjectEnd() {
            this.skipWhitespace();

            char c = this.buffer.get();
            checkState(c == ']', "expected end object character, but found '%c'", c);
        }

        @Override
        public String nextQuotedLatinString() {
            this.skipToNextValue();

            char c = this.buffer.get();
            checkState(c == '"', "expected opening quote, but found '%c'", c);

            StringBuilder builder = new StringBuilder();
            while (true) {
                c = this.buffer.get();
                if (c == '"') {
                    if (this.buffer.charAt(0) == '"') { //escaped doublequote
                        this.buffer.get(); //skip
                        builder.append('"');
                    } else { //end of string
                        break;
                    }
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        @Override
        public String nextEnumName() throws IOException {
            this.skipToNextValue();

            int start = this.buffer.position();
            char c = this.buffer.get();
            checkState(c >= 'A' && c <= 'Z', "not a valid enum start character: '%c'", c);

            while (!isWhitespace(c = this.buffer.get()) && c != ',' && c != ']') {
                checkState((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_', "not a valid enum character: '%c'", c);
            }

            int end = this.buffer.position();
            if (c != ',' && c != ']') {
                this.skipWhitespace();
                checkState((c = this.buffer.get()) == ',' || c == ']', "expected enum termination character, but found '%c'", c);
            }

            //unread last character (which is either ',' or ']')
            this.buffer.position(this.buffer.position() - 1);

            return this.buffer.duplicate().position(start).limit(end - 1).toString();
        }

        private long readUnsignedInteger() {
            char c = this.buffer.get();
            checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);

            long value = 0L;
            do {
                value = addExact(multiplyExact(value, 10L), c - '0');
                c = this.buffer.get();
            } while (c >= '0' && c <= '9');
            this.buffer.position(this.buffer.position() - 1);

            return value;
        }

        private long readSignedInteger() {
            char c = this.buffer.get();
            switch (c) {
                case '-':
                    return -this.readUnsignedInteger();
                default:
                    this.buffer.position(this.buffer.position() - 1);
                case '+':
                    return this.readUnsignedInteger();
            }
        }

        private double readExactNumericLiteral_fractionalPart() {
            int start = this.buffer.position();

            char c = this.buffer.get();
            checkState(c >= '0' && c <= '9', "expected digit, found '%c'", c);

            //seek ahead until we find the last digit
            do {
                c = this.buffer.get();
            } while (c >= '0' && c <= '9');
            this.buffer.position(this.buffer.position() - 1);
            int end = this.buffer.position();

            //parsing floating-point numbers without loss of precision is hard, so i can't be bothered to do it
            return Double.parseDouble("0." + this.buffer.duplicate().position(start).limit(end));
        }

        @Override
        public Number nextUnsignedNumericLiteral() {
            this.skipToNextValue();
            return this.nextUnsignedNumericLiteral0();
        }

        private Number nextUnsignedNumericLiteral0() {
            int start = this.buffer.position();

            char c = this.buffer.get();
            if (c == '.') { //<exact numeric literal>: second case
                return this.readExactNumericLiteral_fractionalPart();
            } else {
                this.buffer.position(this.buffer.position() - 1);
            }

            long value = this.readUnsignedInteger();

            c = this.buffer.get();
            switch (c) {
                case 'E': //<approximate numeric literal>
                    this.readSignedInteger();
                    return Double.parseDouble(this.buffer.duplicate().position(start).limit(this.buffer.position()).toString());
                case '.': //<exact numeric literal>: first case
                    c = this.buffer.get();
                    this.buffer.position(this.buffer.position() - 1);
                    if (c >= '0' && c <= '9') {
                        this.readExactNumericLiteral_fractionalPart();

                        c = this.buffer.get();
                        if (c == 'E') { //<approximate numeric literal>
                            this.readSignedInteger();
                            //return valueAsDouble * pow(10.0d, this.readSignedInteger());
                        } else {
                            this.buffer.position(this.buffer.position() - 1);
                        }
                    } else if (c == 'E') { //<approximate numeric literal>
                        this.buffer.get();
                        this.readSignedInteger();
                        //return valueAsDouble * pow(10.0d, this.readSignedInteger());
                    }
                    return Double.parseDouble(this.buffer.duplicate().position(start).limit(this.buffer.position()).toString());
                default:
                    this.buffer.position(this.buffer.position() - 1);
                    return value;
            }
        }

        @Override
        public Number nextSignedNumericLiteral() throws IOException {
            this.skipToNextValue();

            switch (this.buffer.get()) {
                default:
                    this.buffer.position(this.buffer.position() - 1); //neither plus nor minus, unread previous char and assume positive
                    //fall through
                case '+':
                    //read unsigned number and return it without changes
                    return this.nextUnsignedNumericLiteral0();
                case '-':
                    //read unsigned number, then negate it before returning
                    Number unsigned = this.nextUnsignedNumericLiteral0();
                    if (unsigned instanceof Float) {
                        return -unsigned.floatValue();
                    } else if (unsigned instanceof Double) {
                        return -unsigned.doubleValue();
                    } else if (unsigned instanceof Integer) {
                        return Math.multiplyExact(unsigned.intValue(), -1);
                    } else if (unsigned instanceof Long) {
                        return Math.multiplyExact(unsigned.longValue(), -1L);
                    } else {
                        throw new IllegalStateException(PorkUtil.className(unsigned));
                    }
            }
        }

        @Override
        public void close() {
            //no-op
        }
    }
}
