package io.github.terra121.dataset.osm.poly;

import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.interval.Interval;
import io.github.terra121.util.interval.IntervalTree;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.util.PorkUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public class Polygon implements Bounds2d {
    protected static Segment toSegment(@NonNull double[][] points, int i0, int i1) {
        return new Segment(points[i0][0], points[i0][1], points[i1][0], points[i1][1]);
    }

    public static void main(String... args) {
        BufferedImage img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Polygon polygon = new Polygon(new double[][][]{
                { { 0, 64 }, { 0, 0 }, { 64, 0 }, { 128, 64 }, { 128, 128 }, { 64, 128 } },
                { { 16, 16 }, { 16, 32 }, { 32, 32 }, { 32, 16 } }
        });
        /*polygon = new Polygon(new double[][][]{
                { { 0, 0 }, { 64, 32 }, { 128, 0 }, { 64, 128 }, }
        });*/

        polygon.tessellate(0, 512, 0, 512, 15, (x, z, depth) -> {
            img.setRGB(z, x, 0xFF000000 | (depth << 4 | depth) << 16);
        });

        PorkUtil.simpleDisplayImage(img);
    }

    protected final IntervalTree<Segment> segments;
    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    public Polygon(@NonNull double[][][] shapes) {
        checkArg(shapes.length >= 1, "must provide at least one shape!");
        for (double[][] shape : shapes) {
            checkArg(shape.length >= 3, "a polygon must contain at least 3 points!");
        }

        //compute bounds
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (double[] point : shapes[0]) {
            minX = min(minX, point[0]);
            maxX = max(maxX, point[0]);
            minZ = min(minZ, point[1]);
            maxZ = max(maxZ, point[1]);
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        List<Segment> segments = new ArrayList<>(Arrays.stream(shapes).mapToInt(arr -> arr.length).sum());
        for (double[][] shape : shapes) {
            for (int i = 1; i < shape.length; i++) {
                segments.add(toSegment(shape, i - 1, i));
            }
            segments.add(toSegment(shape, 0, shape.length - 1));
        }

        this.segments = new IntervalTree<>(segments);
    }

    public double[] getIntersectionPoints(int pos) {
        Collection<Segment> segments = this.segments.getAllIntersecting(Interval.of(pos, pos + 1));
        checkState((segments.size() & 1) == 0, "odd number of intersection points?!?");
        double[] arr = new double[segments.size()];

        double center = pos + 0.5d;
        int i = 0;
        for (Segment s : segments) {
            arr[i++] = lerp(s.lat0, s.lat1, (s.lon0 - center) / (s.lon0 - s.lon1));
        }
        Arrays.sort(arr);
        return arr;
    }

    public void tessellate(int baseX, int sizeX, int baseZ, int sizeZ, int maxDist, @NonNull TessellationCallback callback) {
        double[][] intersections2d = new double[sizeX + (maxDist << 1)][];
        for (int x = -maxDist; x < sizeX + maxDist; x++) {
            intersections2d[x + maxDist] = this.getIntersectionPoints(baseX + x);
        }

        for (int x = 0; x < sizeX; x++) {
            double[] intersectionPoints = intersections2d[x + maxDist];

            for (int i = 0; i < intersectionPoints.length; ) {
                int min = clamp(floorI(intersectionPoints[i++]), baseZ, baseZ + sizeZ);
                int max = clamp(floorI(intersectionPoints[i++]), baseZ, baseZ + sizeZ);
                for (int z = min; z < max; z++) {
                    int dist = min(min(z - min, max - z) + 1, maxDist);
                    callback.accept(x + baseX, z, dist);
                }
            }
        }
    }

    /**
     * Callback function used to set individual pixel values during tessellation.
     *
     * @author DaPorkchop_
     */
    @FunctionalInterface
    public interface TessellationCallback {
        void accept(int x, int z, int dist);
    }
}
