package net.buildtheearth.terraplusplus.crs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.cs.CartesianCS;
import net.buildtheearth.terraplusplus.crs.datum.GeodeticDatum;
import net.buildtheearth.terraplusplus.crs.operation.Projection;
import net.buildtheearth.terraplusplus.util.InternHelper;

/**
 * @author DaPorkchop_
 */
@Data
@With(AccessLevel.PRIVATE)
public final class ProjectedCRS implements GeodeticCRS {
    @NonNull
    private final GeographicCRS baseCRS;

    @NonNull
    private final CartesianCS coordinateSystem;

    @NonNull
    private final Projection projection;

    @Override
    public GeodeticDatum datum() {
        return this.baseCRS.datum();
    }

    @Override
    public ProjectedCRS intern() {
        return InternHelper.intern(this.withBaseCRS(this.baseCRS.intern())
                .withCoordinateSystem(this.coordinateSystem.intern())
                .withProjection(this.projection.intern()));
    }
}
