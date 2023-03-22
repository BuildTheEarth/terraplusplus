package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTPrimeMeridian extends WKTObject.WithID {
    public static final WKTParseSchema<WKTPrimeMeridian> PARSE_SCHEMA = WKTParseSchema.builder(WKTPrimeMeridianBuilderImpl::new, WKTPrimeMeridianBuilder::build)
            .permitKeyword("PRIMEM", "PRIMEMERIDIAN")
            .requiredStringProperty(WKTPrimeMeridianBuilder::name)
            .requiredSignedNumericAsDoubleProperty(WKTPrimeMeridianBuilder::irmLongitude)
            .optionalObjectProperty(WKTAngleUnit.PARSE_SCHEMA, WKTPrimeMeridianBuilder::unit)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    private final double irmLongitude;

    @Builder.Default
    private final WKTAngleUnit unit = null; //TODO: "!! If <angle unit> is omitted, the <signed numeric literal> value must be in the CRS's CS angular unit if available, else in decimal degrees."

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PRIMEM")
                .writeQuotedLatinString(this.name)
                .writeSignedNumericLiteral(this.irmLongitude)
                .writeOptionalObject(this.unit)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
