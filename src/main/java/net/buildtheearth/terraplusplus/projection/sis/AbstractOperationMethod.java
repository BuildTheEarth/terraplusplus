package net.buildtheearth.terraplusplus.projection.sis;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractToGeoMathTransform2D;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.unsafe.PUnsafe;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.referencing.operation.DefaultOperationMethod;
import org.apache.sis.referencing.operation.transform.MathTransformProvider;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.util.FactoryException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractOperationMethod extends DefaultOperationMethod implements MathTransformProvider {
    public AbstractOperationMethod(@NonNull ParameterDescriptorGroup parameters) {
        super(ImmutableMap.of(NAME_KEY, parameters.getName()), parameters);
    }

    public AbstractOperationMethod(@NonNull String name, @NonNull GeneralParameterDescriptor... parameters) {
        this(new ParameterBuilder()
                .addName(SISHelper.tppOperationIdentifier(name))
                .createGroup(parameters));
    }

    public static abstract class ForLegacyProjection extends AbstractOperationMethod {
        private static final Class<?> CONCATENATEDTRANSFORM_CLASS = PorkUtil.classForName("org.apache.sis.referencing.operation.transform.ConcatenatedTransform");
        private static final long CONCATENATEDTRANSFORM_INVERSE_OFFSET = PUnsafe.pork_getOffset(CONCATENATEDTRANSFORM_CLASS, "inverse");

        public ForLegacyProjection(@NonNull ParameterDescriptorGroup parameters) {
            super(parameters);
        }

        public ForLegacyProjection(@NonNull String name, @NonNull GeneralParameterDescriptor... parameters) {
            super(name, parameters);
        }

        protected abstract AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException;

        @Override
        @SneakyThrows(NoninvertibleTransformException.class)
        public MathTransform createMathTransform(MathTransformFactory factory, ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException, FactoryException {
            AbstractFromGeoMathTransform2D fromGeoBase = this.createBaseTransform(parameters);
            MathTransform fromGeoComplete = fromGeoBase.completeTransform(factory);

            AbstractToGeoMathTransform2D toGeoBase = fromGeoBase.inverse();
            MathTransform toGeoComplete = toGeoBase.completeTransform(factory);

            if (fromGeoBase == fromGeoComplete) { //no normalization is happening
                checkState(toGeoBase == toGeoComplete, "fromGeo transform isn't normalized, but toGeo is?!?");
            } else if (CONCATENATEDTRANSFORM_CLASS.isInstance(fromGeoComplete)) { //the transform has been concatenated with its normalization and denormalization transforms
                checkState(CONCATENATEDTRANSFORM_CLASS.isInstance(toGeoComplete), "fromGeo transform isn't a ConcatenatedTransform, but toGeo is?!?");

                if (PUnsafe.getObject(fromGeoComplete, CONCATENATEDTRANSFORM_INVERSE_OFFSET) == null) { //the transform's inverse instance hasn't been set yet
                    checkState(PUnsafe.getObject(toGeoComplete, CONCATENATEDTRANSFORM_INVERSE_OFFSET) == null, "fromGeo ConcatenatedTransform doesn't have an inverse, but toGeo does?!?");

                    //set inverses
                    PUnsafe.putObject(fromGeoComplete, CONCATENATEDTRANSFORM_INVERSE_OFFSET, toGeoComplete);
                    PUnsafe.putObject(toGeoComplete, CONCATENATEDTRANSFORM_INVERSE_OFFSET, fromGeoComplete);
                } else {
                    checkState(fromGeoComplete.inverse() == toGeoComplete, "fromGeo's inverse is different than toGeo!");
                }
            } else {
                throw new IllegalStateException(PorkUtil.className(fromGeoComplete) + ": " + fromGeoComplete);
            }

            checkState(fromGeoComplete.inverse() == toGeoComplete);
            checkState(toGeoComplete.inverse() == fromGeoComplete);

            return fromGeoComplete;
        }
    }
}
