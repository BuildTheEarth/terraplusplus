package net.buildtheearth.terraplusplus.projection.sis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.parameter.DefaultParameterValueGroup;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.referencing.ImmutableIdentifier;
import org.apache.sis.referencing.operation.DefaultOperationMethod;
import org.apache.sis.referencing.operation.transform.MathTransformProvider;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.util.FactoryException;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public final class WrappedProjectionOperationMethod extends DefaultOperationMethod implements MathTransformProvider {
    private static final Map<String, ?> PROPERTIES = ImmutableMap.of(
            NAME_KEY, new ImmutableIdentifier(Citations.fromName("Terra++"), "Terra++", "Terra++ Internal Projection"));

    private static final ParameterDescriptor<String> PARAMETER_TYPE = new ParameterBuilder()
            .addName("type")
            .setRequired(true)
            .create(String.class, null);

    private static final ParameterDescriptor<String> PARAMETER_JSON_ARGS = new ParameterBuilder()
            .addName("json_args")
            .setRequired(false)
            .create(String.class, "{}");

    private static final ParameterDescriptorGroup PARAMETERS = new ParameterBuilder()
            .addName((ImmutableIdentifier) PROPERTIES.get(NAME_KEY))
            .createGroup(PARAMETER_TYPE, PARAMETER_JSON_ARGS);

    public WrappedProjectionOperationMethod() {
        super(PROPERTIES, PARAMETERS);
    }

    @Override
    public MathTransform createMathTransform(MathTransformFactory factory, ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException, FactoryException {
        DefaultParameterValueGroup params = (DefaultParameterValueGroup) parameters;

        String typeName = params.stringValue(PARAMETER_TYPE);
        Class<? extends GeographicProjection> type = GlobalParseRegistries.PROJECTIONS.get(typeName);
        if (type == null) {
            throw new InvalidParameterValueException("unknown projection type: \"" + typeName + '"', PARAMETER_TYPE.getName().getCode(), typeName);
        }

        GeographicProjection projection;
        String jsonArgs = params.stringValue(PARAMETER_JSON_ARGS);
        try {
            projection = TerraConstants.JSON_MAPPER.readValue(jsonArgs, type);
        } catch (JsonProcessingException e) {
            throw new InvalidParameterValueException("invalid projection arguments for type \"" + type + "\": \"" + jsonArgs + '"', PARAMETER_JSON_ARGS.getName().getCode(), jsonArgs);
        }

        return new WrappedProjectionMapTransform(projection, true);
    }
}
