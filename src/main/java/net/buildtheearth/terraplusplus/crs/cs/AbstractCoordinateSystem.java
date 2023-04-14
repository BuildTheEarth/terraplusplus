package net.buildtheearth.terraplusplus.crs.cs;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.cs.axis.Axis;
import net.buildtheearth.terraplusplus.crs.cs.axis.AxisDirection;
import net.buildtheearth.terraplusplus.crs.unit.Unit;

import java.util.Objects;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractCoordinateSystem implements CoordinateSystem {
    protected static ImmutableList<Axis> initialValidateAxes(@NonNull ImmutableList<Axis> axes, AbstractCoordinateSystem system) {
        for (int i = 0; i < axes.size(); i++) {
            Axis axis = Objects.requireNonNull(axes.get(i));

            //make sure the axis points in a valid direction
            checkArg(system.permittedDirection(axis.direction()),
                    "illegal axis direction %s for coordinate system", axis.direction());

            //make sure the axis points in a valid direction
            checkArg(system.permittedUnit(axis.direction(), axis.unit()),
                    "illegal axis unit %s for coordinate system axis in direction %s", axis.unit(), axis.direction());

            //make sure there aren't two axes going in the same direction (or in opposite directions)
            for (int j = 0; j < i; j++) {
                Axis otherAxis = axes.get(j);
                checkArg(axis.direction().absolute() != otherAxis.direction().absolute(),
                        "collinear axes in coordinate system: %s, %s", axis, otherAxis);
            }
        }
        return axes;
    }

    protected abstract boolean permittedDirection(@NonNull AxisDirection direction);

    protected abstract boolean permittedUnit(@NonNull AxisDirection direction, @NonNull Unit unit);

    protected void checkDimension(int dimension) {
        int thisDimension = this.dimension(); //checkDimension() should be inlined into callers, so this likely won't cause any dynamic dispatch in practice
        checkArg(thisDimension == dimension, "mismatched argument dimension! expected: %d, given: %d", thisDimension, dimension);
    }
}
