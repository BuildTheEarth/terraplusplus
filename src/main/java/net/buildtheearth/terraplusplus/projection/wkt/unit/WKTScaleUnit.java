package net.buildtheearth.terraplusplus.projection.wkt.unit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#35">WKT Specification §7.4: Unit and unit conversion factor</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Getter
public final class WKTScaleUnit extends WKTObject {
    public static final WKTParseSchema<WKTScaleUnit> PARSE_SCHEMA = WKTParseSchema.builder(WKTScaleUnitBuilderImpl::new, WKTScaleUnitBuilder::build)
            .permitKeyword("SCALEUNIT")
            .requiredStringProperty(WKTScaleUnit.WKTScaleUnitBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTScaleUnit.WKTScaleUnitBuilder::conversionFactor)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    /**
     * The scale factor.
     */
    private final double conversionFactor;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("SCALEUNIT")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.conversionFactor);
        if (this.id() != null) {
            this.id().write(writer);
        }
        writer.endObject();
    }
}
