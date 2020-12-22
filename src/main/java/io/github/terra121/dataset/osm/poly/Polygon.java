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
        while (true) {
            debugThing();
        }
    }

    private static void debugThing() { //separate method to allow me to hot-swap
        int size = 130;
        int shift = 2;
        BufferedImage img = new BufferedImage(size << shift, size << shift, BufferedImage.TYPE_INT_ARGB);
        Polygon polygon = new Polygon(new double[][][]{
                { { 0, 64 }, { 8, 8 }, { 64, 0 }, { 128, 64 }, { 128, 128 }, { 64, 128 } },
                { { 16, 16 }, { 16, 32 }, { 32, 32 }, { 32, 16 } }
        });
            /*polygon = new Polygon(new double[][][]{
                    { { 0, 0 }, { 64, 32 }, { 128, 0 }, { 64, 128 }, }
            });*/

        int[] arr = new int[1 << shift];

        polygon.tessellateDistance(0, size, 0, size, 15, (x, z, depth) -> {
            checkIndex(16, abs(depth));
            int color = (depth & 0xF) == depth ? 0xFF000000 | (depth << 4 | depth) << 16 : 0xFF0000FF;
            Arrays.fill(arr, color);
            img.setRGB(z << shift, x << shift, 1 << shift, 1 << shift, arr, 0, 0);
        });

        //fill base polygon shape with green
        polygon.tessellateShape(0, size, 0, size, (x, z) -> {
            int color = img.getRGB(z << shift, x << shift) | 0xFF00FF00;
            Arrays.fill(arr, color);
            img.setRGB(z << shift, x << shift, 1 << shift, 1 << shift, arr, 0, 0);
        });

        PorkUtil.simpleDisplayImage(true, img);
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

    public void tessellateDistance(int baseX, int sizeX, int baseZ, int sizeZ, int maxDist, @NonNull DistTessellationCallback callback) {
        int[][] distances2d = new int[(maxDist << 1) + 1][sizeZ];

        for (int x = -maxDist; x < sizeX + maxDist; x++) {
            { //compute distances
                int[] distances = distances2d[0];
                Arrays.fill(distances, 0);
                double[] intersectionPoints = this.getIntersectionPoints(x + baseX);

                for (int i = 0; i < intersectionPoints.length; ) {
                    int min = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                    int max = clamp(ceilI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                    for (int z = min; z < max; z++) {
                        distances[z] = min(min(z - min, max - z) + 1, maxDist);
                    }
                }

                System.arraycopy(distances2d, 1, distances2d, 0, maxDist << 1); //shift distances down by one
                distances2d[maxDist << 1] = distances;
            }

            if (x >= maxDist) {
                int[] distances = distances2d[maxDist];
                for (int z = 0; z < sizeZ; z++) {
                    int dist = distances[z];
                    if (dist > 0) { //if distance is <= 0, the pixel is blank, so we don't care about the distance
                        int r = dist;
                        for (int dx = -r; dx <= r; dx++) {
                            dist = min(dist, distances2d[dx + maxDist][z] + abs(dx));
                        }
                        callback.pixel(x + baseX - maxDist, z + baseZ, dist);
                    }
                }
            }
        }
    }

    public void tessellateShape(int baseX, int sizeX, int baseZ, int sizeZ, @NonNull ShapeTessellationCallback callback) {
        for (int x = 0; x < sizeX; x++) {
            double[] intersectionPoints = this.getIntersectionPoints(x + baseX);

            for (int i = 0; i < intersectionPoints.length; ) {
                int min = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                int max = clamp(ceilI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                for (int z = min; z < max; z++) {
                    callback.pixel(x + baseX, z);
                }
            }
        }
    }

    /**
     * Callback function used by {@link #tessellateDistance(int, int, int, int, int, DistTessellationCallback)}.
     *
     * @author DaPorkchop_
     */
    @FunctionalInterface
    public interface DistTessellationCallback {
        void pixel(int x, int z, int dist);
    }

    /**
     * Callback function used by {@link #tessellateShape(int, int, int, int, ShapeTessellationCallback)}.
     *
     * @author DaPorkchop_
     */
    @FunctionalInterface
    public interface ShapeTessellationCallback {
        void pixel(int x, int z);
    }
}
