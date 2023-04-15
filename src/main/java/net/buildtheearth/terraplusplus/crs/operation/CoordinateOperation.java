package net.buildtheearth.terraplusplus.crs.operation;

import net.buildtheearth.terraplusplus.crs.CRS;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface CoordinateOperation extends Internable<CoordinateOperation> {
    CRS sourceCRS();

    CRS targetCRS();

    String operationVersion();

    //TODO: Extent extent();

    String scope();

    //TODO: MathTransform transform();
}
