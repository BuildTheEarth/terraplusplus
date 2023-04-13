package net.buildtheearth.terraplusplus.crs;

import com.google.common.collect.ImmutableList;
import net.buildtheearth.terraplusplus.crs.axis.Axis;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface CRS extends Internable<CRS> {
    /**
     * @return a {@link ImmutableList list} of the {@link Axis axes} which make up this coordinate system
     */
    ImmutableList<Axis> axes();
}
