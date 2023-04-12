package net.buildtheearth.terraplusplus.crs;

import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface CRS extends Internable<CRS> {
    /**
     * @return the number of coordinate axes in this system
     */
    int dim();
}
