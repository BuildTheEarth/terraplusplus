package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Destination for for serializing WKT-based projections.
 *
 * @author DaPorkchop_
 */
public interface WKTWriter extends AutoCloseable {
    /**
     * Begins writing a WKT object with the given keyword.
     *
     * @param keyword the keyword
     */
    WKTWriter beginObject(@NonNull String keyword) throws IOException;

    /**
     * Signifies the end of a WKT object.
     */
    WKTWriter endObject() throws IOException;

    /**
     * Writes the given {@link String} as a quoted latin string.
     *
     * @param string the {@link String}
     */
    WKTWriter writeQuotedLatinString(@NonNull String string) throws IOException;

    /**
     * Writes the given {@link Number} as an unsigned numeric literal.
     *
     * @param number the {@link Number}
     */
    WKTWriter writeUnsignedNumericLiteral(@NonNull Number number) throws IOException;

    /**
     * Writes the given {@link Number} as a signed numeric literal.
     *
     * @param number the {@link Number}
     */
    WKTWriter writeSignedNumericLiteral(@NonNull Number number) throws IOException;

    /**
     * Writes the given {@link String} as an enum value.
     *
     * @param value the {@link String}
     */
    WKTWriter writeEnumName(@NonNull String value) throws IOException;

    /**
     * Writes the given {@link Enum} as an enum value.
     *
     * @param value the {@link Enum}
     */
    default WKTWriter writeEnum(@NonNull Enum<?> value) throws IOException {
        return this.writeEnumName(value.name());
    }

    default WKTWriter writeRequiredObject(@NonNull WKTObject object) throws IOException {
        object.write(this);
        return this;
    }

    default WKTWriter writeOptionalObject(WKTObject object) throws IOException {
        if (object != null) {
            object.write(this);
        }
        return this;
    }

    default WKTWriter writeObjectList(@NonNull Iterable<? extends WKTObject> objects) throws IOException {
        for (WKTObject object : objects) {
            object.write(this);
        }
        return this;
    }

    /**
     * Signifies that this writer has finished writing all objects.
     */
    @Override
    void close() throws IOException;

    /**
     * Implementation of {@link WKTWriter} which delegates to an {@link Appendable} given at construction time.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    class ToAppendable implements WKTWriter {
        @NonNull
        protected final Appendable target;

        @NonNull
        protected final WKTStyle style;

        protected int depth = 0;
        protected boolean writtenFirstElementInObject = false;
        protected boolean shouldPrefixNextValueWithNewline = false;

        protected boolean hasStartedObject() {
            return this.depth > 0;
        }

        protected void beforeWriteValue() throws IOException {
            checkState(this.hasStartedObject(), "not currently writing an object!");

            if (!this.writtenFirstElementInObject) {
                this.writtenFirstElementInObject = true;
            } else {
                this.target.append(',');

                if (!this.shouldPrefixNextValueWithNewline) {
                    this.target.append(this.style.afterComma());
                }
            }
            if (this.shouldPrefixNextValueWithNewline) {
                this.shouldPrefixNextValueWithNewline = false;
                this.writeLineBreakAndIndent();
            }
        }

        protected void writeLineBreakAndIndent() throws IOException {
            this.target.append(this.style.lineBreak());
            for (int i = 0; i < this.depth; i++) {
                this.target.append(this.style.indent());
            }
        }

        @Override
        public WKTWriter beginObject(@NonNull String keyword) throws IOException {
            checkArg(!keyword.isEmpty() && keyword.matches("[A-Z][A-Z0-9_]*"), "illegal WKT keyword '%s'", keyword);

            if (this.hasStartedObject()) { //this isn't the root object, add some indentation
                this.shouldPrefixNextValueWithNewline = true;
                this.beforeWriteValue();
            }

            this.target.append(keyword).append('[');
            this.depth++;
            this.writtenFirstElementInObject = false;
            return this;
        }

        @Override
        public WKTWriter endObject() throws IOException {
            checkState(this.hasStartedObject(), "not currently writing an object!");

            this.depth--;
            this.target.append(']');
            this.shouldPrefixNextValueWithNewline = true;
            return this;
        }

        @Override
        public WKTWriter writeQuotedLatinString(@NonNull String string) throws IOException {
            this.beforeWriteValue();
            this.target.append('"').append(string.replace("\"", "\"\"")).append('"');
            return this;
        }

        @Override
        public WKTWriter writeUnsignedNumericLiteral(@NonNull Number number) throws IOException {
            this.beforeWriteValue();

            /*String string = number.toString();
            int start = 0;
            int end = string.length();

            if (string.length() > ".0".length() && string.endsWith(".0")) { //skip redundant trailing decimal which is appended to floating-point numbers for some reason
                end = string.length() - ".0".length();
            }
            this.target.append(string, start, end);*/

            if ((number instanceof Float || number instanceof Double) && (true || (Math.round(number.doubleValue()) == number.doubleValue() && Math.abs(number.doubleValue()) < 1E10d))) {
                NumberFormat format = NumberFormat.getInstance(Locale.ROOT);
                format.setGroupingUsed(false);
                format.setMinimumFractionDigits(1);
                format.setMaximumFractionDigits(100);
                //this.target.append(String.format("%.1f", number));
                this.target.append(format.format(number));
            } else {
                this.target.append(number.toString());
            }
            return this;
        }

        @Override
        public WKTWriter writeSignedNumericLiteral(@NonNull Number number) throws IOException {
            return this.writeUnsignedNumericLiteral(number);
        }

        @Override
        public WKTWriter writeEnumName(@NonNull String value) throws IOException {
            this.beforeWriteValue();
            this.target.append(value);
            return this;
        }

        @Override
        public void close() throws IOException {
            checkState(this.depth == 0, "unclosed object!");
        }
    }
}