package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

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
        protected final CharSequence lineBreak;
        @NonNull
        protected final CharSequence indent;
        @NonNull
        protected final CharSequence afterComma;

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
                    this.target.append(this.afterComma);
                }
            }
            if (this.shouldPrefixNextValueWithNewline) {
                this.shouldPrefixNextValueWithNewline = false;
                this.writeLineBreakAndIndent();
            }
        }

        protected void writeLineBreakAndIndent() throws IOException {
            this.target.append(this.lineBreak);
            for (int i = 0; i < this.depth; i++) {
                this.target.append(this.indent);
            }
        }

        @Override
        public WKTWriter beginObject(@NonNull String keyword) throws IOException {
            checkArg(!keyword.isEmpty() && keyword.matches("[A-Z][A-Z0-9]*"), "illegal WKT keyword '%s'", keyword);

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
            this.target.append(number.toString()); //TODO
            return this;
        }

        @Override
        public void close() throws IOException {
            checkState(this.depth == 0, "unclosed object!");
        }
    }
}
