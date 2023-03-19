package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.With;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#143">WKT Specification §C.2: Backward compatibility of CRS common attributes</a>
 */
@Builder
@Data
@With
public final class WKTID {
    public static final WKTParseSchema<WKTID> PARSE_SCHEMA = WKTParseSchema.builder(WKTID::builder, WKTIDBuilder::build)
            .permitKeyword("ID", "AUTHORITY")
            .requiredStringProperty(WKTIDBuilder::authorityName)
            .addSimpleProperty(
                    reader -> reader.peek() == WKTReader.Token.QUOTED_LATIN_STRING ? reader.nextQuotedLatinString() : reader.nextUnsignedNumericLiteral(),
                    WKTIDBuilder::authorityUniqueIdentifier, true)
            .build();

    @NonNull
    private final String authorityName;

    @NonNull
    private final Object authorityUniqueIdentifier;

    @Override
    @SneakyThrows(IOException.class)
    public String toString() {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, " ", "", " ")) {
            this.write(writer);
        }
        return builder.toString();
    }

    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ID")
                .writeQuotedLatinString(this.authorityName);
        if (this.authorityUniqueIdentifier instanceof Number) {
            writer.writeUnsignedNumericLiteral((Number) this.authorityUniqueIdentifier);
        } else {
            writer.writeQuotedLatinString(this.authorityUniqueIdentifier.toString());
        }
        writer.endObject();
    }
}
