package net.buildtheearth.terraplusplus.projection.dymaxion;

import com.google.common.collect.ImmutableMap;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.parameter.DefaultParameterDescriptorGroup;
import org.apache.sis.referencing.ImmutableIdentifier;
import org.apache.sis.referencing.operation.DefaultOperationMethod;
import org.apache.sis.referencing.operation.transform.MathTransformProvider;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.util.FactoryException;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class DymaxionProjectionOperationMethod extends DefaultOperationMethod implements MathTransformProvider {
    private static final String CITATION_NAME = "Terra++";
    private static final Citation CITATION = Citations.fromName(CITATION_NAME);

    private static final String NAME = CITATION_NAME + " Dymaxion";

    private static final Map<String, ?> PROPERTIES = ImmutableMap.of(
            DefaultParameterDescriptorGroup.NAME_KEY, new ImmutableIdentifier(CITATION, CITATION_NAME, NAME));

    private static final DefaultParameterDescriptorGroup PARAMETERS = new DefaultParameterDescriptorGroup(PROPERTIES, 1, 1);

    public DymaxionProjectionOperationMethod() {
        super(PROPERTIES, PARAMETERS);
    }

    @Override
    public MathTransform createMathTransform(MathTransformFactory factory, ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException, FactoryException {
        return new DymaxionProjectionMathTransform(true);
    }
}
