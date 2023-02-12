package net.buildtheearth.terraplusplus.dataset.vector.geometry.polygon;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.jackson.IntRange;

import java.util.Arrays;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A custom polygon rasterizer which not only fills the polygon area, but computes the distance from the edge of the polygon - both inside and out - up to a given maximum
 * distance.
 *
 * @author DaPorkchop_
 */
public final class DistancePolygon extends AbstractPolygon {
    protected final int maxDist;

    public DistancePolygon(@NonNull String id, double layer, @NonNull DrawFunction draw, IntRange levels, @NonNull MultiPolygon polygons, int maxDist) {
        super(id, layer, draw, levels, polygons);

        this.maxDist = positive(maxDist, "maxDist");
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int tileX, int tileZ, int zoom, @NonNull Bounds2d bounds) {
        if (!this.containsZoom(zoom)) {
            return;
        }

        int baseX = Coords.cubeToMinBlock(tileX << zoom);
        int baseZ = Coords.cubeToMinBlock(tileZ << zoom);

        int maxDist = this.maxDist;
        int[][] distances2d = new int[(maxDist << 1) + 1][16];

        for (int x = -maxDist; x < 16 + maxDist; x++) {
            DISTANCES:
            { //compute distances
                int[] distances = distances2d[0];
                System.arraycopy(distances2d, 1, distances2d, 0, maxDist << 1); //shift distances down by one
                distances2d[maxDist << 1] = distances;
                double[] intersectionPoints = this.getIntersectionPoints((x << zoom) + baseX);

                if (intersectionPoints.length == 0) { //no intersections along this line
                    Arrays.fill(distances, Integer.MIN_VALUE);
                    break DISTANCES;
                }

                int i = Arrays.binarySearch(intersectionPoints, baseZ - maxDist);
                i ^= i >> 31; //convert possibly negative insertion index to positive
                i -= i & 1; //ensure we start on an even index

                int max;

                if (i < intersectionPoints.length) { //index is valid
                    int mask = 0;
                    int min = (floorI(intersectionPoints[i++]) - baseZ) >> zoom;
                    int limit = baseX + (16 << zoom);

                    //fill everything up to this point with blanks
                    for (int z = 0, itrMax = clamp(min, 0, 16); z < itrMax; z++) {
                        distances[z] = z - min;
                    }

                    do {
                        max = (floorI(intersectionPoints[i++]) - baseZ) >> zoom;

                        for (int z = clamp(min, 0, 16), itrMax = clamp(max, 0, 16); z < itrMax; z++) {
                            distances[z] = min(z - min, max - z - 1) ^ mask;
                        }

                        min = max;
                        mask = ~mask;
                    } while (i < intersectionPoints.length && max <= limit);
                } else { //index is too high, simply fill from the end
                    max = (floorI(intersectionPoints[intersectionPoints.length - 1]) - baseZ) >> zoom;
                }

                //fill everything to the edge with blanks
                for (int z = clamp(max, 0, 16); z < 16; z++) {
                    distances[z] = max - z - 1;
                }
            }

            if (x >= maxDist) {
                int[] distances = distances2d[maxDist];
                for (int z = 0; z < 16; z++) {
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
                    if (dist >= -maxDist) {
                        this.draw.drawOnto(builder, x - maxDist, z, min(dist, maxDist));
                    }
                }
            }
        }
    }

    @Override
    public double minX() {
        return super.minX() - this.maxDist;
    }

    @Override
    public double maxX() {
        return super.maxX() + this.maxDist;
    }

    @Override
    public double minZ() {
        return super.minZ() - this.maxDist;
    }

    @Override
    public double maxZ() {
        return super.maxZ() + this.maxDist;
    }
}
