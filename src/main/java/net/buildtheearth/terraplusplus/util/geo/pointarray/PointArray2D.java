package net.buildtheearth.terraplusplus.util.geo.pointarray;

import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.geometry.Envelope2D;

/**
 * Specialization of {@link PointArray} for arrays of two-dimensional points.
 *
 * @author DaPorkchop_
 */
public interface PointArray2D extends PointArray {
    @Override
    default @Positive int pointDimensions() {
        return 2;
    }

    @Override
    default @NotNegative int totalValueSize() {
        return Math.multiplyExact(this.size(), 2);
    }

    @Override
    Envelope2D envelope();
}
