package net.buildtheearth.terraplusplus.crs.operation;

import net.buildtheearth.terraplusplus.crs.CRS;
import net.buildtheearth.terraplusplus.crs.datum.Datum;

/**
 * An {@link Operation} where the source and target {@link CRS}s are based on different {@link Datum}s.
 *
 * @author DaPorkchop_
 */
public interface Transformation extends Operation {
    @Override
    Transformation intern();
}
