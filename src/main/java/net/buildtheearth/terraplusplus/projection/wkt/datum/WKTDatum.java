package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Getter
public final class WKTDatum extends WKTObject {
    @NonNull
    private final String name;

    @NonNull
    private final WKTEllipsoid ellipsoid;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
    }
}
