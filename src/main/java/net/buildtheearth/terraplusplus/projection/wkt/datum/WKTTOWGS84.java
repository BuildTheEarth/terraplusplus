package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTTOWGS84 extends WKTObject {
    public static final WKTParseSchema<WKTTOWGS84> PARSE_SCHEMA = WKTParseSchema.builder(WKTTOWGS84BuilderImpl::new, WKTTOWGS84Builder::build)
            .permitKeyword("TOWGS84")
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::dx)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::dy)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::dz)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::ex)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::ey)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::ez)
            .requiredSignedNumericAsDoubleProperty(WKTTOWGS84Builder::ppm)
            .build();

    private final double dx;
    private final double dy;
    private final double dz;
    private final double ex;
    private final double ey;
    private final double ez;
    private final double ppm;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("TOWGS84")
                .writeSignedNumericLiteral(this.dx)
                .writeSignedNumericLiteral(this.dy)
                .writeSignedNumericLiteral(this.dz)
                .writeSignedNumericLiteral(this.ex)
                .writeSignedNumericLiteral(this.ey)
                .writeSignedNumericLiteral(this.ez)
                .writeSignedNumericLiteral(this.ppm)
                .endObject();
    }
}
