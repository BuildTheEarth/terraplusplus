package net.buildtheearth.terraplusplus.crs.operation.parameter;

import net.buildtheearth.terraplusplus.crs.operation.OperationMethod;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * Describes a single parameter to an {@link OperationMethod}.
 * <p>
 * Defined as {@code CC_OperationParameter} in ISO 19111.
 *
 * @author DaPorkchop_
 */
public interface SingleOperationParameter extends Internable<SingleOperationParameter> {
    /**
     * @return this parameter's name
     */
    String name();

    /**
     * @return {@code true} if a value for this parameter must be provided
     */
    boolean required();
}
