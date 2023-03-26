package net.buildtheearth.terraplusplus.projection.wkt.misc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTReader;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#143">WKT Specification §C.2: Backward compatibility of CRS common attributes</a>
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTID extends AbstractWKTObject {
    public static final WKTParseSchema<WKTID> PARSE_SCHEMA = WKTParseSchema.builder(WKTIDBuilderImpl::new, WKTIDBuilder::build)
            .permitKeyword("ID", "AUTHORITY")
            .requiredStringProperty(WKTIDBuilder::authority)
            .addSimpleProperty(
                    reader -> reader.peek() == WKTReader.Token.QUOTED_LATIN_STRING ? reader.nextQuotedLatinString() : reader.nextUnsignedNumericLiteral(),
                    WKTIDBuilder::code, true)
            .build();

    @NonNull
    private final String authority;

    @NonNull
    private final Object code;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ID")
                .writeQuotedLatinString(this.authority);
        if (this.code instanceof Number) {
            writer.writeUnsignedNumericLiteral((Number) this.code);
        } else {
            writer.writeQuotedLatinString(this.code.toString());
        }
        writer.endObject();
    }
}
