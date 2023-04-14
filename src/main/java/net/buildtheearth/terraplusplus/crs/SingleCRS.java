package net.buildtheearth.terraplusplus.crs;

import net.buildtheearth.terraplusplus.crs.cs.CoordinateSystem;
import net.buildtheearth.terraplusplus.crs.datum.Datum;

/**
 * @author DaPorkchop_
 */
public interface SingleCRS extends CRS {
    /**
     * @return the {@link CoordinateSystem coordinate system} used by this CRS
     */
    CoordinateSystem coordinateSystem();

    /**
     * @return the {@link Datum} used by this CRS
     */
    Datum datum();

    @Override
    SingleCRS intern();
}
