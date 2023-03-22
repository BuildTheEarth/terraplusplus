package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjection;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjectionParameter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTProjectedCRS extends AbstractWKTCRS {
    public static final WKTParseSchema<WKTProjectedCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTProjectedCRSBuilderImpl::new, WKTProjectedCRSBuilder::build)
            .permitKeyword("PROJCS")
            .requiredStringProperty(WKTProjectedCRSBuilder::name)
            .requiredObjectProperty(WKTGeographicCRS.PARSE_SCHEMA, WKTProjectedCRSBuilder::geogcs)
            .requiredObjectProperty(WKTProjection.PARSE_SCHEMA, WKTProjectedCRSBuilder::projection)
            .addObjectListProperty(WKTProjectionParameter.PARSE_SCHEMA, WKTProjectedCRSBuilder::projectionParameter, true)
            .requiredObjectProperty(WKTLengthUnit.PARSE_SCHEMA, WKTProjectedCRSBuilder::axisUnit)
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTProjectedCRSBuilder::axis, true)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTGeographicCRS geogcs;

    @NonNull
    private final WKTProjection projection;

    @NonNull
    @Singular
    private final ImmutableList<WKTProjectionParameter> projectionParameters;

    @NonNull
    private final WKTLengthUnit axisUnit;

    @NonNull
    @Singular("axis")
    private final ImmutableList<WKTAxis> axes;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PROJCS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.geogcs)
                .writeRequiredObject(this.projection)
                .writeObjectList(this.projectionParameters)
                .writeRequiredObject(this.axisUnit)
                .writeObjectList(this.axes)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
