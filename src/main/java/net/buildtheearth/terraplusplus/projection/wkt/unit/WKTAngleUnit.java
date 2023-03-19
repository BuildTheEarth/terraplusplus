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
public final class WKTAngleUnit extends WKTObject {
    public static final WKTAngleUnit DEGREE = new WKTAngleUnit("degree", 0.0174532925199433d);

    public static final WKTParseSchema<WKTAngleUnit> PARSE_SCHEMA = WKTParseSchema.builder(WKTAngleUnitBuilderImpl::new, WKTAngleUnitBuilder::build)
            .permitKeyword("ANGLEUNIT")
            .requiredStringProperty(WKTAngleUnit.WKTAngleUnitBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTAngleUnit.WKTAngleUnitBuilder::conversionFactor)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();
    
    @NonNull
    private final String name;

    /**
     * The number of radians per unit.
     */
    private final double conversionFactor;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ANGLEUNIT")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.conversionFactor);
        if (this.id() != null) {
            this.id().write(writer);
        }
        writer.endObject();
    }
}
