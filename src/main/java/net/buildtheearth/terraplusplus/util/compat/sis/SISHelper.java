package net.buildtheearth.terraplusplus.util.compat.sis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.daporkchop.lib.common.pool.array.ArrayAllocator;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.cs.CoordinateSystems;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.projection.ProjectionException;
import org.apache.sis.referencing.operation.transform.IterationStrategy;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.util.Arrays;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class SISHelper {
    @SuppressWarnings("deprecation")
    private static final LoadingCache<GeographicProjection, CoordinateReferenceSystem> PROJECTION_TO_CRS_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues()
            .build(CacheLoader.from(GeographicProjection::projectedCRS));

    public static CoordinateReferenceSystem projectedCRS(@NonNull GeographicProjection projection) {
        return PROJECTION_TO_CRS_CACHE.getUnchecked(projection);
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

    @SneakyThrows(TransformException.class)
    private static void transformSinglePointWithOutOfBoundsNaN(MathTransform transform, double[] src, int srcOff, double[] dst, int dstOff, int dstDim) {
        try {
            transform.transform(src, srcOff, dst, dstOff, 1);

            if (!isAnyPossibleOutOfBoundsValue(dst, dstOff, dstOff + dstDim)) {
                return;
            }
        } catch (ProjectionException e) {
            //silently swallow exception, we'll fill dst with NaN values instead
        }

        Arrays.fill(dst, dstOff, dstOff + dstDim, Double.NaN);
    }

    public static void transformManyPointsWithOutOfBoundsNaN(@NonNull MathTransform transform, double[] src, int srcOff, double[] dst, int dstOff, int count) {
        processWithIterationStrategy(
                (src1, srcOff1, srcDim, dst1, dstOff1, dstDim) -> transformSinglePointWithOutOfBoundsNaN(transform, src1, srcOff1, dst1, dstOff1, dstDim),
                src, srcOff, transform.getSourceDimensions(), dst, dstOff, transform.getTargetDimensions(), count);
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
