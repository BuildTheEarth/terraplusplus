package net.buildtheearth.terraplusplus.util.compat.sis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.misc.threadlocal.TL;
import net.daporkchop.lib.common.pool.array.ArrayAllocator;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.internal.system.DataDirectory;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.ImmutableIdentifier;
import org.apache.sis.referencing.cs.CoordinateSystems;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.projection.ProjectionException;
import org.apache.sis.referencing.operation.transform.IterationStrategy;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class SISHelper {
    static {
        //suppress 'The “SIS_DATA” environment variable is not set.' warnings in the log
        DataDirectory.quiet();
    }

    private static final GeographicCRS TPP_GEO_CRS = CommonCRS.WGS84.normalizedGeographic();

    /**
     * The {@link CoordinateReferenceSystem} used by Terra++ for geographic coordinates.
     */
    public static GeographicCRS tppGeoCrs() {
        return TPP_GEO_CRS;
    }

    private static final Citation TPP_CITATION = Citations.fromName("Terra++");

    public static Citation tppCitation() {
        return TPP_CITATION;
    }

    public static ImmutableIdentifier tppOperationIdentifier(@NonNull String name) {
        return new ImmutableIdentifier(TPP_CITATION, "Terra++", ("Terra++ " + name).intern());
    }

    private static final TL<ReferencingFactoryContainer> FACTORIES = TL.initializedWith(ReferencingFactoryContainer::new);

    public static ReferencingFactoryContainer factories() {
        return FACTORIES.get();
    }

    private static final Cache<GeographicProjection, CoordinateReferenceSystem> PROJECTION_TO_CRS_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues()
            .build();

    @SuppressWarnings("deprecation")
    @SneakyThrows(ExecutionException.class)
    public static CoordinateReferenceSystem projectedCRS(@NonNull GeographicProjection projection) {
        if (projection instanceof GeographicProjection.FastProjectedCRS) { //projectedCRS() is fast, we can bypass the cache
            return projection.projectedCRS();
        }

        CoordinateReferenceSystem crs = PROJECTION_TO_CRS_CACHE.getIfPresent(projection);
        if (crs != null) { //corresponding CRS instance was already cached
            return crs;
        }

        //get the CRS instance, store it in the cache and return it.
        //  because calling projection.projectedCRS() might cause recursive calls to this function, and i'm not sure if guava caches supports reentrant loaders,
        //  we get the projected CRS outside of the cache and then try to insert it, returning any existing values if another thread beats us to it.
        CoordinateReferenceSystem realCrs = Objects.requireNonNull(projection.projectedCRS());
        return PROJECTION_TO_CRS_CACHE.get(projection, () -> realCrs);
    }

    @SneakyThrows(FactoryException.class)
    public static CoordinateOperation findOperation(CoordinateReferenceSystem source, CoordinateReferenceSystem target) {
        return CRS.findOperation(source, target, null);
    }

    public static boolean isPossibleOutOfBoundsValue(double value) {
        return Double.isInfinite(value) || Double.isNaN(value);
    }

    private static boolean isAnyPossibleOutOfBoundsValue(double[] values, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (isPossibleOutOfBoundsValue(values[i])) {
                return true;
            }
        }
        return false;
    }

    public static void transformSinglePointWithOutOfBoundsNaN(@NonNull MathTransform transform, double[] src, int srcOff, double[] dst, int dstOff) {
        transformSinglePointWithOutOfBoundsNaN(transform, src, srcOff, dst, dstOff, transform.getTargetDimensions());
    }

    private static void transformSinglePointWithOutOfBoundsNaN(MathTransform transform, double[] src, int srcOff, double[] dst, int dstOff, int dstDim) {
        try {
            transform.transform(src, srcOff, dst, dstOff, 1);

            if (!isAnyPossibleOutOfBoundsValue(dst, dstOff, dstOff + dstDim)) {
                return;
            }
        } catch (TransformException e) {
            //silently swallow exception, we'll fill dst with NaN values instead
        }

        Arrays.fill(dst, dstOff, dstOff + dstDim, Double.NaN);
    }

    private static boolean rangesOverlap(@NotNegative int off0, @NotNegative int len0, @NotNegative int off1, @NotNegative int len1) {
        return off0 < off1 + len1 && off1 < off0 + len0;
    }

    public static void transformManyPointsWithOutOfBoundsNaN(@NonNull MathTransform transform, double[] src, int srcOff, double[] dst, int dstOff, int count) {
        final int srcDim = transform.getSourceDimensions();
        final int dstDim = transform.getTargetDimensions();

        ArrayAllocator<double[]> alloc = null;
        double[] tempArray = null;

        //noinspection ArrayEquality
        if (src == dst && rangesOverlap(srcOff, count * srcDim, dstOff, count * dstDim)) {
            //the source and destination ranges overlap, back up all the source values to a temporary array so we can restore them if one of the transform steps
            //  fails without setting NaNs
            alloc = DOUBLE_ALLOC.get();
            tempArray = alloc.atLeast(count * srcDim);
            System.arraycopy(src, srcOff, tempArray, 0, count * srcDim);
        }

        TRANSFORM_COMPLETE:
        {
            try {
                //try to transform everything, assuming there won't be any failures
                transform.transform(src, srcOff, dst, dstOff, count);
            } catch (TransformException e) {
                SLOW_FALLBACK:
                {
                    MathTransform lastCompletedTransform = e.getLastCompletedTransform();
                    if (lastCompletedTransform == null) { //the transform didn't try to transform every point and set failed ones to NaN, no way to continue without going element-by-element
                        break SLOW_FALLBACK;
                    }

                    List<MathTransform> steps = MathTransforms.getSteps(transform);
                    int i = steps.indexOf(lastCompletedTransform); //find the index of the last completed step in the list of all steps
                    checkState(i >= 0, "transform step '%s' isn't present in transform '%s'!", lastCompletedTransform, transform);
                    checkState(steps.lastIndexOf(lastCompletedTransform) == i, "transform step '%s' is present in transform '%s' more than once!", lastCompletedTransform, transform);

                    //try to execute all remaining transform steps
                    for (i++; i < steps.size(); i++) {
                        try {
                            steps.get(i).transform(dst, dstOff, dst, dstOff, count);
                        } catch (TransformException e1) {
                            if (e1.getLastCompletedTransform() != steps.get(i)) { //the transform didn't try to transform every point and set failed ones to NaN, no way to continue
                                break SLOW_FALLBACK;
                            }
                        }
                    }
                    break TRANSFORM_COMPLETE;
                }

                //one of the transforms failed without setting the last completed transform, so the destination array contains unknown data.
                //  we'll fall back to transforming one element at a time, restoring the original source values from the backup made at the start if necessary
                if (tempArray != null) { //restore source from backup
                    src = tempArray;
                    srcOff = 0;
                }

                //transform one element at a time
                processWithIterationStrategy(
                        (src1, srcOff1, srcDim1, dst1, dstOff1, dstDim1) -> transformSinglePointWithOutOfBoundsNaN(transform, src1, srcOff1, dst1, dstOff1, dstDim1),
                        src, srcOff, srcDim, dst, dstOff, dstDim, count);
            }
        }

        if (tempArray != null) { //we allocated a temporary array, so we should release it again
            alloc.release(tempArray);
        }
    }

    private static void processWithIterationStrategy(TransformElementProcessor action, double[] src, int srcOff, int srcDim, double[] dst, int dstOff, int dstDim, int count) {
        ArrayAllocator<double[]> alloc = null;
        double[] tempArray = null;

        double[] dstFinal = null;
        int dstFinalOff = 0;

        int srcInc = srcDim;
        int dstInc = dstDim;
        switch (IterationStrategy.suggest(srcOff, srcDim, dstOff, dstDim, count)) {
            case ASCENDING:
                break;
            case DESCENDING:
                srcOff += (count - 1) * srcInc;
                dstOff += (count - 1) * dstInc;
                srcInc = -srcInc;
                dstInc = -dstInc;
                break;
            case BUFFER_SOURCE: //allocate a new buffer and copy the source values into it
                alloc = DOUBLE_ALLOC.get();
                tempArray = alloc.atLeast(count * srcInc);
                System.arraycopy(src, srcOff, tempArray, 0, count * srcInc);

                src = tempArray;
                srcOff = 0;
                break;
            case BUFFER_TARGET: //allocate a new buffer and configure everything to write the destination values into it once everything else has been processed
                alloc = DOUBLE_ALLOC.get();
                tempArray = alloc.atLeast(count * dstInc);

                dstFinal = dst;
                dstFinalOff = dstOff;

                dst = tempArray;
                dstOff = 0;
                break;
        }

        for (int i = 0, currSrcOff = srcOff, currDstOff = dstOff; i < count; i++, currSrcOff += srcInc, currDstOff += dstInc) {
            action.process(src, currSrcOff, srcDim, dst, currDstOff, dstDim);
        }

        if (dstFinal != null) { //copy the values into the real destination array
            System.arraycopy(dst, dstOff, dstFinal, dstFinalOff, count * dstInc);
        }

        if (tempArray != null) { //a temporary array was used, release it
            alloc.release(tempArray);
        }
    }

    @FunctionalInterface
    private interface TransformElementProcessor {
        void process(double[] src, int srcOff, int srcDim, double[] dst, int dstOff, int dstDim);
    }

    //TODO: i'm fairly certain this'll need special handling for large envelopes on dymaxion-based projections
    public static GeneralEnvelope transform(CoordinateOperation operation, Envelope envelope) throws TransformException {
        return Envelopes.transform(operation, envelope);
    }

    public static Bounds2d toBounds(@NonNull Envelope envelope) {
        checkArg(envelope.getDimension() == 2);
        return Bounds2d.of(envelope.getMinimum(0), envelope.getMaximum(0), envelope.getMinimum(1), envelope.getMaximum(1));
    }

    public static MatrixSIS getAxisOrderMatrix(@NonNull CoordinateOperation operation) {
        return Matrices.createTransform(CoordinateSystems.getAxisDirections(operation.getSourceCRS().getCoordinateSystem()), CoordinateSystems.getAxisDirections(operation.getTargetCRS().getCoordinateSystem()));
    }
}
