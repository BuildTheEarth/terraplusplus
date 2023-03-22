package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#143">WKT Specification §C.2: Backward compatibility of CRS common attributes</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTID extends WKTObject {
    public static final WKTParseSchema<WKTID> PARSE_SCHEMA = WKTParseSchema.builder(WKTIDBuilderImpl::new, WKTIDBuilder::build)
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
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("AUTHORITY")
                .writeQuotedLatinString(this.authorityName);
        if (this.authorityUniqueIdentifier instanceof Number) {
            writer.writeUnsignedNumericLiteral((Number) this.authorityUniqueIdentifier);
        } else {
            writer.writeQuotedLatinString(this.authorityUniqueIdentifier.toString());
        }
        writer.endObject();
    }
}
