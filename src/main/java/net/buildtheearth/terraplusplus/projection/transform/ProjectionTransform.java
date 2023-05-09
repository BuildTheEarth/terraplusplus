package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.daporkchop.lib.common.util.PorkUtil;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.internal.referencing.provider.Affine;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.util.FactoryException;

import java.util.stream.IntStream;

/**
 * Warps a Geographic projection and applies a transformation to it.
 */
@Getter(onMethod_ = { @JsonGetter, @JsonSerialize(as = GeographicProjection.class) })
public abstract class ProjectionTransform implements GeographicProjection {
    protected final GeographicProjection delegate;

    /**
     * @param delegate - projection to transform
     */
    public ProjectionTransform(GeographicProjection delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean upright() {
        return this.delegate.upright();
    }

    @Override
    public abstract double[] bounds();

    @Override
    public double[] boundsGeo() {
        return this.delegate.boundsGeo();
    }

    protected abstract String toSimpleString();

    protected abstract MatrixSIS affineMatrix();

    protected CoordinateSystemAxis[] transformAxes(CoordinateSystemAxis[] axes) {
        return axes;
    }

    @Override
    @SneakyThrows(FactoryException.class)
    public CoordinateReferenceSystem projectedCRS() {
        /*GeographicProjection proj = this;
        Matrix3 affineMatrix = new Matrix3();
        do {
            affineMatrix.multiply(((ProjectionTransform) proj).affineMatrix());
        } while ((proj = ((ProjectionTransform) proj).delegate()) instanceof ProjectionTransform);*/

        CoordinateReferenceSystem baseCRS = SISHelper.projectedCRS(this.delegate());

        String simpleString = "Terra++ " + this.toSimpleString();
        MatrixSIS affineMatrix = this.affineMatrix();
        if (affineMatrix.isIdentity()) {
            return baseCRS;
        }

        ReferencingFactoryContainer factories = SISHelper.factories();

        CoordinateSystemAxis[] axes = IntStream.range(0, baseCRS.getCoordinateSystem().getDimension()).mapToObj(baseCRS.getCoordinateSystem()::getAxis).toArray(CoordinateSystemAxis[]::new);
        CoordinateSystemAxis[] transformedAxes = this.transformAxes(axes);

        factories.getMathTransformFactory().createAffineTransform(affineMatrix);

        CoordinateSystem cs;
        if (baseCRS.getCoordinateSystem() instanceof CartesianCS) {
            cs = factories.getCSFactory().createCartesianCS(
                    ImmutableMap.of(IdentifiedObject.NAME_KEY, baseCRS.getCoordinateSystem().getName().getCode() + " / " + simpleString),
                    transformedAxes[0], transformedAxes[1]);
        } else if (baseCRS.getCoordinateSystem() instanceof EllipsoidalCS) {
            cs = factories.getCSFactory().createEllipsoidalCS(
                    ImmutableMap.of(IdentifiedObject.NAME_KEY, baseCRS.getCoordinateSystem().getName().getCode() + " / " + simpleString),
                    transformedAxes[0], transformedAxes[1]);
        } else {
            throw new IllegalArgumentException(PorkUtil.className(baseCRS.getCoordinateSystem()));
        }

        return factories.getCRSFactory().createDerivedCRS(
                ImmutableMap.of(IdentifiedObject.NAME_KEY, baseCRS.getName().getCode() + " / " + simpleString),
                baseCRS,
                factories.getCoordinateOperationFactory().createDefiningConversion(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, simpleString),
                        Affine.getProvider(2, 2, true),
                        Affine.parameters(affineMatrix)),
                cs);
    }
}
