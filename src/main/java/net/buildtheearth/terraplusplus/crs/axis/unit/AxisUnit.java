package net.buildtheearth.terraplusplus.crs.axis.unit;

import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface AxisUnit extends Internable<AxisUnit> {
    /**
     * @return the type of value measured by this unit
     */
    UnitType type();

    /**
     * @return the unscaled unit which this unit is derived from
     */
    AxisUnit baseUnit();
}
