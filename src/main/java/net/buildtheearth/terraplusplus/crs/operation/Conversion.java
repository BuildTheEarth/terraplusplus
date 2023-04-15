package net.buildtheearth.terraplusplus.crs.operation;

import net.buildtheearth.terraplusplus.crs.CRS;
import net.buildtheearth.terraplusplus.crs.datum.Datum;

/**
 * An {@link Operation} where both the source and target {@link CRS}s are based on the same {@link Datum}.
 *
 * @author DaPorkchop_
 */
public interface Conversion extends Operation {
    /**
     * @deprecated this property isn't used by {@link Conversion}
     */
    @Override
    @Deprecated
    default String operationVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    Conversion intern();
}
