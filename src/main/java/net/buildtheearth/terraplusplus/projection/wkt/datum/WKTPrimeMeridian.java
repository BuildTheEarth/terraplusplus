package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Getter
public final class WKTPrimeMeridian extends WKTObject {
    public static final WKTParseSchema<WKTPrimeMeridian> PARSE_SCHEMA = WKTParseSchema.builder(WKTPrimeMeridianBuilderImpl::new, WKTPrimeMeridianBuilder::build)
            .permitKeyword("PRIMEM", "PRIMEMERIDIAN")
            .requiredStringProperty(WKTPrimeMeridianBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTPrimeMeridianBuilder::irmLongitude)
            .optionalObjectProperty(WKTAngleUnit.PARSE_SCHEMA, WKTPrimeMeridianBuilder::unit)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    private final double irmLongitude;

    @NonNull
    @Builder.Default
    private final WKTAngleUnit unit = WKTAngleUnit.DEGREE; //TODO: "!! If <angle unit> is omitted, the <signed numeric literal> value must be in the CRS's CS angular unit if available, else in decimal degrees."

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PRIMEM")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.irmLongitude);
        //noinspection ConstantValue
        if (this.unit != null) {
            this.unit.write(writer);
        }
        if (this.id() != null) {
            this.id().write(writer);
        }
        writer.endObject();
    }
}
