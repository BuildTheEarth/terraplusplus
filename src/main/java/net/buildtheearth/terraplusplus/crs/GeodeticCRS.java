package net.buildtheearth.terraplusplus.crs;

import net.buildtheearth.terraplusplus.crs.datum.GeodeticDatum;

/**
 * @author DaPorkchop_
 */
public interface GeodeticCRS extends SingleCRS {
    @Override
    GeodeticDatum datum();

    @Override
    GeodeticCRS intern();
}
