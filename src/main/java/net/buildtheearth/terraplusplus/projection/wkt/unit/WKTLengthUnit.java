package net.buildtheearth.terraplusplus.projection.wkt.unit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#35">WKT Specification §7.4: Unit and unit conversion factor</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTLengthUnit extends WKTUnit {
    public static final WKTLengthUnit METRE = builder().name("metre").conversionFactor(1.0d).build();

    public static final WKTParseSchema<WKTLengthUnit> PARSE_SCHEMA = WKTParseSchema.builder(WKTLengthUnitBuilderImpl::new, WKTLengthUnitBuilder::build)
            .permitKeyword("LENGTHUNIT", "UNIT")
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    /**
     * The number of meters per unit.
     */
    @Override
    public double conversionFactor() {
        return super.conversionFactor();
    }

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("LENGTHUNIT")
                .writeQuotedLatinString(this.name())
                .writeUnsignedNumericLiteral(this.conversionFactor())
                .writeOptionalObject(this.id())
                .endObject();
    }
}
