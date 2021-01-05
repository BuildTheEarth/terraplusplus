package io.github.terra121.dataset.osm.poly;

import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.interval.IntervalTree;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.util.math.MathHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public class OSMPolygon implements Bounds2d, Comparable<OSMPolygon> {
    protected static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    protected static Segment toSegment(@NonNull double[][] points, int i0, int i1) {
        return new Segment(points[i0][0], points[i0][1], points[i1][0], points[i1][1]);
    }

    public static void main(String... args) {
        while (true) {
            debugThing();
        }
    }

    private static void debugThing() { //separate method to allow me to hot-swap
        int size = 135;
        int shift = 2;
        BufferedImage img = new BufferedImage(size << shift, size << shift, BufferedImage.TYPE_INT_ARGB);
        OSMPolygon polygon = new OSMPolygon(new double[][][]{
                { { -16, 64 }, { 8, 8 }, { 64, -16 }, { 128, 64 }, { 140, 72 }, { 128, 128 }, { 72, 140 }, { 64, 128 } },
                //{ { 16, 16 }, { 16, 32 }, { 32, 32 }, { 32, 16 } }
                { { 16.5, 16.5 }, { 16.5, 31 }, { 32.5, 32.5 }, { 32.5, 15 } }
        });
        /*polygon = new Polygon(new double[][][]{
                { { 0, 0 }, { 64, 32 }, { 128, 0 }, { 64, 128 }, }
        });*/

        int[] arr = new int[1 << shift];

        polygon.rasterizeDistance(0, size, 0, size, 3, (x, z, depth) -> {
            checkArg(depth >= -3 && depth <= 3, depth);
            int cShift = depth < 0 ? 0 : 16;
            depth ^= depth >> 31;
            depth = abs(depth) << 2 | abs(depth);
            int color = 0xFF000000 | (depth << 4 | depth) << cShift;
            Arrays.fill(arr, color);
            img.setRGB(z << shift, x << shift, 1 << shift, 1 << shift, arr, 0, 0);
        });

        //fill base polygon shape with green
        polygon.rasterizeShape(0, size, 0, size, (x, z) -> {
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

    public OSMPolygon(@NonNull double[][][] shapes) {
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
            if (point != null) {
                minX = min(minX, point[0]);
                maxX = max(maxX, point[0]);
                minZ = min(minZ, point[1]);
                maxZ = max(maxZ, point[1]);
            }
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        List<Segment> segments = new ArrayList<>(Arrays.stream(shapes).mapToInt(arr -> arr.length).sum());
        for (double[][] shape : shapes) {
            double[] prev = shape[0];
            for (int i = 1; i < shape.length; i++) {
                double[] point = shape[i];
                segments.add(new Segment(prev[0], prev[1], point[0], point[1]));
                prev = point;
            }
        }

        checkArg(segments.size() >= 3, "polygon must contain at least 3 valid segments!");

        this.segments = new IntervalTree<>(segments);
    }

    public double[] getIntersectionPoints(int pos) {
        int retries = 0;
        double offset = 0.5d;
        do {
            double center = pos + offset;
            List<Segment> segments = this.segments.getAllIntersecting(center);
            if ((segments.size() & 1) == 0) { //if there's an even count, this was successful
                int size = segments.size();
                if (size == 0) {
                    return EMPTY_DOUBLE_ARRAY;
                } else {
                    double[] arr = new double[size];

                    int i = 0;
                    for (Segment s : segments) {
                        arr[i++] = lerp(s.lat0, s.lat1, (s.lon0 - center) / (s.lon0 - s.lon1));
                    }
                    Arrays.sort(arr);

                    return arr;
                }
            }

            //retry with another random offset
            //this happens because sometimes segments are exactly aligned to the sample grid, in which case everything breaks
            offset = 0.45d + ThreadLocalRandom.current().nextDouble() * 0.1d;
        } while (retries++ < 3);

        //retried multiple times with different offsets and it still failed, abort...
        return EMPTY_DOUBLE_ARRAY;
    }

    public void rasterizeDistance(int baseX, int sizeX, int baseZ, int sizeZ, int maxDist, @NonNull DistRasterizationCallback callback) {
        int[][] distances2d = new int[(maxDist << 1) + 1][sizeZ];

        for (int x = -maxDist; x < sizeX + maxDist; x++) {
            DISTANCES:
            { //compute distances
                int[] distances = distances2d[0];
                System.arraycopy(distances2d, 1, distances2d, 0, maxDist << 1); //shift distances down by one
                distances2d[maxDist << 1] = distances;
                double[] intersectionPoints = this.getIntersectionPoints(x + baseX);

                if (intersectionPoints.length == 0) { //no intersections along this line
                    break DISTANCES;
                }

                int i = Arrays.binarySearch(intersectionPoints, baseZ - maxDist);
                i ^= i >> 31; //convert possibly negative insertion index to positive
                i -= i & 1; //ensure we start on an even index

                int max;

                if (i < intersectionPoints.length) { //index is valid
                    final double end = baseZ + sizeZ + maxDist;

                    int mask = 0;
                    int min = floorI(intersectionPoints[i++]) - baseZ;

                    //fill everything up to this point with blanks
                    for (int z = 0, itrMax = clamp(min, 0, sizeZ); z < itrMax; z++) {
                        distances[z] = z - min;
                    }

                    do {
                        max = floorI(intersectionPoints[i++]) - baseZ;

                        for (int z = clamp(min, 0, sizeZ), itrMax = clamp(max, 0, sizeZ); z < itrMax; z++) {
                            distances[z] = min(z - min, max - z - 1) ^ mask;
                        }

                        min = max;
                        mask = ~mask;
                    } while (i < intersectionPoints.length && intersectionPoints[i] <= end);
                } else { //index is too high, simply fill from the end
                    max = floorI(intersectionPoints[intersectionPoints.length - 1]) - baseZ;
                }

                //fill everything to the edge with blanks
                for (int z = clamp(max, 0, sizeZ); z < sizeZ; z++) {
                    distances[z] = max - z - 1;
                }
            }

            if (x >= maxDist) {
                int[] distances = distances2d[maxDist];
                for (int z = 0; z < sizeZ; z++) {
                    int dist = distances[z];
                    int r = abs(dist);
                    for (int dx = max(-r, -maxDist), maxDx = min(r, maxDist); dx <= maxDx; dx++) {
                        if (dx != 0) {
                            int d2 = distances2d[dx + maxDist][z];
                            if (dist > 0) {
                                /*if (d2 >= 0) {
                                    d2 = d2 + abs(dx);
                                } else {
                                    d2 = abs(dx) - 1;
                                }*/
                                d2 = (d2 & ~(d2 >> 31)) + abs(dx) - (d2 >>> 31);
                            } else {
                                /*if (d2 >= 0) {
                                    d2 = -abs(dx);
                                } else {
                                    d2 = (d2 ^ ~(d2 >> 31)) - abs(dx);
                                }*/
                                d2 = (d2 & (d2 >> 31)) - abs(dx);
                            }
                            if (abs(d2) < abs(dist)) {
                                dist = d2;
                            }
                        }
                    }
                    callback.pixel(x + baseX - maxDist, z + baseZ, clamp(dist, -maxDist, maxDist));
                }
            }
        }
    }

    public void rasterizeShape(int baseX, int sizeX, int baseZ, int sizeZ, @NonNull ShapeRasterizationCallback callback) {
        for (int x = 0; x < sizeX; x++) {
            double[] intersectionPoints = this.getIntersectionPoints(x + baseX);

            for (int i = 0; i < intersectionPoints.length; ) {
                int min = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                int max = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, sizeZ);
                for (int z = min; z < max; z++) {
                    callback.pixel(x + baseX, z + baseZ);
                }
            }
        }
    }

    public void rasterizeOutline(int baseX, int sizeX, int baseZ, int sizeZ, @NonNull ShapeRasterizationCallback callback) {
        this.segments.forEach(s -> {
            double radius = 1.0d;
            double radiusSq = radius * radius;

            double lon0 = s.lon0 - baseX;
            double lon1 = s.lon1 - baseX;
            double lat0 = s.lat0 - baseZ;
            double lat1 = s.lat1 - baseZ;

            int minX = max((int) floor(min(lon0, lon1) - radius), 0);
            int maxX = min((int) ceil(max(lon0, lon1) + radius), sizeX);
            int minZ = max((int) floor(min(lat0, lat1) - radius), 0);
            int maxZ = min((int) ceil(max(lat0, lat1) + radius), sizeZ);

            double segmentLengthSq = (lon1 - lon0) * (lon1 - lon0) + (lat1 - lat0) * (lat1 - lat0);
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double r = ((x - lon0) * (lon1 - lon0) + (z - lat0) * (lat1 - lat0)) / segmentLengthSq;
                    r = MathHelper.clamp(r, 0.0d, 1.0d);

                    double dx = MathUtil.lerp(r, lon0, lon1) - x;
                    double dz = MathUtil.lerp(r, lat0, lat1) - z;
                    if (dx * dx + dz * dz < radiusSq) {
                        callback.pixel(x + baseX, z + baseZ);
                    }
                }
            }
        });
    }

    @Override
    public int compareTo(OSMPolygon o) {
        //TODO: implement this
        return 0;
    }

    /**
     * Callback function used by {@link #rasterizeDistance(int, int, int, int, int, DistRasterizationCallback)}.
     *
     * @author DaPorkchop_
     */
    @FunctionalInterface
    public interface DistRasterizationCallback {
        void pixel(int x, int z, int dist);
    }

    /**
     * Callback function used by {@link #rasterizeShape(int, int, int, int, ShapeRasterizationCallback)}.
     *
     * @author DaPorkchop_
     */
    @FunctionalInterface
    public interface ShapeRasterizationCallback {
        void pixel(int x, int z);
    }
}
