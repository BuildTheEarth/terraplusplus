package net.buildtheearth.terraplusplus.crs.operation;

import com.google.common.collect.ImmutableMap;
import net.buildtheearth.terraplusplus.crs.operation.parameter.SingleOperationParameter;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * Defined as {@code CC_OperationMethod} in ISO 19111.
 *
 * @author DaPorkchop_
 */
public interface OperationMethod extends Internable<OperationMethod> {
    /**
     * @return the name(s) of the formula(s) used by this operation method
     */
    String formula();

    /**
     * @return the number of dimensions in the operation method's source CRS
     */
    int sourceDimensions();

    /**
     * @return the number of dimensions in the operation method's target CRS
     */
    int targetDimensions();

    /**
     * @return a {@link ImmutableMap} of this operation's parameters
     * @apiNote this would be equivalent to {@code CC_OperationParameterGroup} in ISO 19111
     */
    //TODO: maybe use OperationParameters instead?
    ImmutableMap<String, SingleOperationParameter> parameters();
}
