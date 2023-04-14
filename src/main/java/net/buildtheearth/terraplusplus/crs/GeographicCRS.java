package net.buildtheearth.terraplusplus.crs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.cs.EllipsoidalCS;
import net.buildtheearth.terraplusplus.crs.datum.GeodeticDatum;
import net.buildtheearth.terraplusplus.util.InternHelper;

/**
 * @author DaPorkchop_
 */
@Data
@With(AccessLevel.PRIVATE)
public final class GeographicCRS implements GeodeticCRS {
    @NonNull
    private final GeodeticDatum datum;

    @NonNull
    private final EllipsoidalCS coordinateSystem;

    @Override
    public GeographicCRS intern() {
        return InternHelper.intern(this.withDatum(this.datum.intern())
                .withCoordinateSystem(this.coordinateSystem.intern()));
    }
}
