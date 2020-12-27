package io.github.terra121.dataset.osm.segment;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.cache.CachedChunkData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.*;

/**
 * An algorithm used for filling blocks based on the shape of a segment.
 *
 * @author DaPorkchop_
 */
public enum SegmentFill {
    NONE {
        @Override
        public void fill(CachedChunkData data, CubePrimer primer, Segment s, int cubeX, int cubeY, int cubeZ) {
            //no-op
        }
    },
    WIDE {
        @Override
        public void fill(CachedChunkData data, CubePrimer primer, Segment s, int cubeX, int cubeY, int cubeZ) {
            IBlockState state = s.type.state();
            boolean allowInWater = s.type.allowInWater();

            double radius = s.type.computeRadius(s.lanes);
            double radiusSq = radius * radius;

            double lon0 = s.lon0 - Coords.cubeToMinBlock(cubeX);
            double lon1 = s.lon1 - Coords.cubeToMinBlock(cubeX);
            double lat0 = s.lat0 - Coords.cubeToMinBlock(cubeZ);
            double lat1 = s.lat1 - Coords.cubeToMinBlock(cubeZ);

            int minX = max((int) floor(min(lon0, lon1) - radius), 0);
            int maxX = min((int) ceil(max(lon0, lon1) + radius), 16);
            int minZ = max((int) floor(min(lat0, lat1) - radius), 0);
            int maxZ = min((int) ceil(max(lat0, lat1) + radius), 16);

            double segmentLengthSq = (lon1 - lon0) * (lon1 - lon0) + (lat1 - lat0) * (lat1 - lat0);
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (!allowInWater && data.wateroffs[x * 16 + z] > 0.0d) {
                        continue; //don't generate in water
                    }

                    int y = (int) floor(data.heights[x * 16 + z]) - Coords.cubeToMinBlock(cubeY);
                    if ((y & 0xF) != y) { //if not in this range, someone else will handle it
                        continue;
                    }

                    double r = ((x - lon0) * (lon1 - lon0) + (z - lat0) * (lat1 - lat0)) / segmentLengthSq;
                    r = MathHelper.clamp(r, 0.0d, 1.0d);

                    double dx = MathUtil.lerp(r, lon0, lon1) - x;
                    double dz = MathUtil.lerp(r, lat0, lat1) - z;
                    if (dx * dx + dz * dz < radiusSq) {
                        primer.setBlockState(x, y, z, state);
                    }
                }
            }
        }
    },
    NARROW {
        @Override
        public void fill(CachedChunkData data, CubePrimer primer, Segment s, int cubeX, int cubeY, int cubeZ) {
            IBlockState state = s.type.state();
            boolean allowInWater = s.type.allowInWater();

            double start = s.lon0;
            double end = s.lon1;

            if (start > end) {
                double tmp = start;
                start = end;
                end = tmp;
            }

            int sx = max((int) floor(start) - cubeX * 16, 0);
            int ex = min((int) floor(end) - cubeX * 16, 15);

            for (int x = max(sx, 0); x <= ex; x++) {
                double realx = (x + cubeX * 16);
                if (realx < start) {
                    realx = start;
                }

                double nextx = realx + 1;
                if (nextx > end) {
                    nextx = end;
                }

                int from = (int) floor((s.slope * realx + s.offset)) - cubeZ * 16;
                int to = (int) floor((s.slope * nextx + s.offset)) - cubeZ * 16;

                if (from > to) {
                    int tmp = from;
                    from = to;
                    to = tmp;
                }

                to = min(to, 15);

                for (int z = max(0, from); z <= to; z++) {
                    if (!allowInWater && data.wateroffs[x * 16 + z] > 0.0) {
                        continue; //don't generate in water
                    }

                    int y = (int) floor(data.heights[x * 16 + z]) - Coords.cubeToMinBlock(cubeY);
                    if ((y & 0xF) == y) {
                        primer.setBlockState(x, y, z, state);
                    }
                }
            }
        }
    };

    public abstract void fill(CachedChunkData data, CubePrimer primer, Segment s, int cubeX, int cubeY, int cubeZ);
}
