package net.buildtheearth.terraplusplus.crs.operation.parameter;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.DoubleWithUnit;
import net.buildtheearth.terraplusplus.crs.unit.Unit;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * Defined as {@code CC_ParameterValue} in ISO 19111.
 *
 * @author DaPorkchop_
 */
public interface SingleOperationParameterValue extends Internable<SingleOperationParameterValue> {
    /**
     * @return the {@link SingleOperationParameter} whose value is provided by this object
     */
    SingleOperationParameter parameter();

    /**
     * @return the parameter value with associated unit
     */
    DoubleWithUnit doubleValue();

    /**
     * @return the parameter value converted to the given {@link Unit}
     */
    double doubleValue(@NonNull Unit unit);

    int intValue();

    boolean booleanValue();

    String stringValue();
}
