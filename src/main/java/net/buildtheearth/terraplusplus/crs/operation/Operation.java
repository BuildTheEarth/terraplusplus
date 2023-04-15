package net.buildtheearth.terraplusplus.crs.operation;

import com.google.common.collect.ImmutableMap;
import net.buildtheearth.terraplusplus.crs.operation.parameter.SingleOperationParameterValue;

/**
 * Defined as {@code CC_Operation} in ISO 19111.
 *
 * @author DaPorkchop_
 */
public interface Operation extends SingleOperation {
    /**
     * @return a {@link ImmutableMap} of this operation's parameters
     * @apiNote this would be equivalent to {@code CC_OperationParameterGroup} in ISO 19111
     */
    //TODO: maybe use OperationParameterValues instead?
    ImmutableMap<String, SingleOperationParameterValue> parameterValues();

    @Override
    Operation intern();
}
