package net.buildtheearth.terraplusplus.crs;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.cs.CartesianCS;
import net.buildtheearth.terraplusplus.crs.cs.CoordinateSystem;
import net.buildtheearth.terraplusplus.crs.datum.GeodeticDatum;
import net.buildtheearth.terraplusplus.util.InternHelper;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@With(AccessLevel.PRIVATE)
public final class GeocentricCRS implements GeodeticCRS {
    @NonNull
    private final GeodeticDatum datum;

    @NonNull
    private final CoordinateSystem coordinateSystem;

    public GeocentricCRS(@NonNull GeodeticDatum datum, @NonNull CartesianCS coordinateSystem) {
        this(datum, (CoordinateSystem) coordinateSystem);
    }

    //TODO: also allow spherical coordinate systems

    @Override
    public GeocentricCRS intern() {
        return InternHelper.intern(this.withDatum(this.datum.intern())
                .withCoordinateSystem(this.coordinateSystem.intern()));
    }
}
