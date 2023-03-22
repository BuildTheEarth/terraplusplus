package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.CharBuffer;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Data
@Builder(toBuilder = true)
public final class WKTStyle {
    public static final WKTStyle DENSE = new WKTStyle("", "", "");

    public static final WKTStyle ONE_LINE = new WKTStyle(" ", "", " ");

    public static final WKTStyle PRETTY = new WKTStyle("\n", "    ", " ");

    @NonNull
    private final CharSequence lineBreak;
    @NonNull
    private final CharSequence indent;
    @NonNull
    private final CharSequence afterComma;

    @SneakyThrows(IOException.class)
    public String format(@NonNull CharSequence wkt) {
        StringBuilder builder = new StringBuilder();
        try (WKTReader reader = new WKTReader.FromCharBuffer(CharBuffer.wrap(wkt));
             WKTWriter writer = new WKTWriter.ToAppendable(builder, this)) {
            int depth = 0;

            do {
                WKTReader.Token token = reader.peek();

                switch (token) {
                    case BEGIN_OBJECT:
                        writer.beginObject(reader.nextKeyword());
                        depth++;
                        break;
                    case END_OBJECT:
                        reader.nextObjectEnd();
                        writer.endObject();
                        depth--;
                        break;
                    case ENUM:
                        writer.writeEnumName(reader.nextEnumName());
                        break;
                    case QUOTED_LATIN_STRING:
                        writer.writeQuotedLatinString(reader.nextQuotedLatinString());
                        break;
                    case NUMBER:
                        writer.writeSignedNumericLiteral(reader.nextSignedNumericLiteral());
                        break;
                    default:
                        throw new IllegalStateException(token.name());
                }
            } while (depth > 0);
        }
        return builder.toString();
    }
}
