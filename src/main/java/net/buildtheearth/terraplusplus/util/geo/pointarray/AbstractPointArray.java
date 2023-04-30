package net.buildtheearth.terraplusplus.util.geo.pointarray;

import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractPointArray implements PointArray {
    private final PointArray parent;
    @NonNull
    private final CoordinateReferenceSystem crs;

    private final int size;

    public AbstractPointArray(PointArray parent, @NonNull CoordinateReferenceSystem crs, @NotNegative int size) {
        this.parent = parent;
        this.crs = crs;
        this.size = notNegative(size, "size");
    }
}
