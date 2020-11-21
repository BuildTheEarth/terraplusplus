package io.github.terra121.dataset.osm.segment;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.EarthTerrainProcessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import static java.lang.Math.*;

/**
 * An algorithm used for filling blocks based on the shape of a segment.
 *
 * @author DaPorkchop_
 */
public enum SegmentFill {
    NONE {
        @Override
        public void fill(EarthTerrainProcessor gen, Segment s, World world, int cubeX, int cubeY, int cubeZ) {
            //no-op
        }
    },
    WIDE {
        private double bound(double x, double slope, double j, double k, double r, double x0, double b, double sign) {
            double slopesign = sign * signum(slope);

            if (x < j - slopesign * x0) { //left circle
                return slope * j + sign * Math.sqrt(r * r - (x - j) * (x - j));
            } else if (x > k - slopesign * x0) { //right circle
                return slope * k + sign * Math.sqrt(r * r - (x - k) * (x - k));
            } else {
                return slope * x + sign * b;
            }
        }

        @Override
        public void fill(EarthTerrainProcessor gen, Segment s, World world, int cubeX, int cubeY, int cubeZ) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            SegmentType type = s.type;
            IBlockState state = type.state();
            double r = type.computeRadius(s.lanes);

            double[] heights = gen.heightsCache.getUnchecked(new ChunkPos(cubeX, cubeZ));

            double x0 = 0;
            double b = r;
            if (Math.abs(s.slope) >= 0.000001d) {
                x0 = r / Math.sqrt(1 + 1 / (s.slope * s.slope));
                b = (s.slope < 0 ? -1 : 1) * x0 * (s.slope + 1.0 / s.slope);
            }

            double minLon = s.lon0 - (cubeX * 16);
            double maxLon = s.lon1 - (cubeX * 16);
            double off = s.offset - (cubeZ * 16) + s.slope * (cubeX * 16);

            if (minLon > maxLon) {
                double t = minLon;
                minLon = maxLon;
                maxLon = t;
            }

            double ij = minLon - r;
            double ik = maxLon + r;

            minLon = max(minLon, 0.0d);
            maxLon = min(maxLon, 16.0d);

            int is = (int) floor(ij);
            int ie = (int) floor(ik);

            for (int x = is; x <= ie; x++) {
                double ul = bound(x, s.slope, minLon, maxLon, r, x0, b, 1) + off; //TODO: save these repeated values
                double ur = bound((double) x + 1, s.slope, minLon, maxLon, r, x0, b, 1) + off;
                double ll = bound(x, s.slope, minLon, maxLon, r, x0, b, -1) + off;
                double lr = bound((double) x + 1, s.slope, minLon, maxLon, r, x0, b, -1) + off;

                double from = min(min(ul, ur), min(ll, lr));
                double to = max(max(ul, ur), max(ll, lr));

                if (!Double.isNaN(from)) {
                    int ifrom = max((int) floor(from), -15);
                    int ito = min((int) floor(to), 31);

                    for (int z = ifrom; z <= ito; z++) {
                        //get the part of the center line i am tangent to (i hate high school algebra!!!)
                        double mainX = x;
                        if (Math.abs(s.slope) >= 0.000001d) {
                            mainX = ((double) z + (double) x / s.slope - off) / (s.slope + 1 / s.slope);
                        }

                        double mainZ = s.slope * mainX + off;

                        //double[] geo = gen.projection.toGeo(mainX + cubeX * 16, mainZ + cubeZ * 16);
                        double[] geo = gen.projection.toGeo(cubeX * 16 + x, cubeZ * 16 + z);
                        int y = (int) floor(gen.heights.estimateLocal(geo[0], geo[1])) - cubeY * 16;
                        //int y = (int) floor(heights[x * 16 + z]) - Coords.cubeToMinBlock(cubeY);

                        if (y >= 0 && y < 16) { //if not in this range, someone else will handle it
                            world.setBlockState(pos.setPos(cubeX * 16 + x, cubeY * 16 + y, cubeZ * 16 + z), state);

                            //clear the above blocks (to a point, we don't want to be here all day)
                            /*IBlockState defState = Blocks.AIR.getDefaultState();
                            for (int ay = y + 1; ay < 16 * 2 && world.getBlockState(new BlockPos(x + cubeX * 16, ay + cubeY * 16, z + cubeZ * 16)) != defState; ay++) {
                                world.setBlockState(new BlockPos(x + cubeX * 16, ay + cubeY * 16, z + cubeZ * 16), defState);
                            }*/
                        }
                    }
                }
            }
        }
    },
    NARROW {
        @Override
        public void fill(EarthTerrainProcessor gen, Segment s, World world, int cubeX, int cubeY, int cubeZ) {
            if (s.type == SegmentType.ROAD || s.type == SegmentType.MINOR
                || s.type == SegmentType.STREAM || s.type == SegmentType.BUILDING) {
                IBlockState state = s.type.state();
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

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
                        double[] geo = gen.projection.toGeo(cubeX * 16 + x, cubeZ * 16 + z);
                        int y = (int) floor(gen.heights.estimateLocal(geo[0], geo[1])) - Coords.cubeToMinBlock(cubeY);

                        if (y >= 0 && y < 16) {
                            world.setBlockState(pos.setPos(cubeX * 16 + x, cubeY * 16 + y, cubeZ * 16 + z), state);
                        }
                    }
                }
            }
        }
    };

    public abstract void fill(EarthTerrainProcessor gen, Segment s, World world, int cubeX, int cubeY, int cubeZ);
}
