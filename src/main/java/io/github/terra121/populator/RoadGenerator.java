package io.github.terra121.populator;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.terra121.EarthTerrainProcessor;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.projection.GeographicProjection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;
import java.util.Set;

import static java.lang.Math.*;

@RequiredArgsConstructor
public class RoadGenerator implements ICubicPopulator {
    private static double bound(double x, double slope, double j, double k, double r, double x0, double b, double sign) {
        double slopesign = sign * (slope < 0 ? -1 : 1);

        if (x < j - slopesign * x0) { //left circle
            return slope * j + sign * Math.sqrt(r * r - (x - j) * (x - j));
        }
        if (x > k - slopesign * x0) { //right circle
            return slope * k + sign * Math.sqrt(r * r - (x - k) * (x - k));
        }
        return slope * x + sign * b;
    }

    @NonNull
    protected final EarthTerrainProcessor gen;

    @Override
    public void generate(World world, Random rand, CubePos pos, Biome biome) {
        int cubeX = pos.getX();
        int cubeY = pos.getY();
        int cubeZ = pos.getZ();

        Set<Segment> segments = this.gen.osm.chunkStructures(cubeX, cubeZ);

        if (segments != null) {
            // rivers done before roads
            /*for (OpenStreetMap.Edge e : edges) {
                switch (e.type) {
                    case RIVER:
                        this.placeEdge(e, world, cubeX, cubeY, cubeZ, 5, (dis, bpos) -> this.riverState(world, dis, bpos));
                        break;
                    case STREAM:
                        this.placeEdge(e, world, cubeX, cubeY, cubeZ, 1, (dis, bpos) -> this.riverState(world, dis, bpos));
                        break;
                }
            }*/

            // (1+w)l+l is the equation to calculate road width, where "w" is the width and "l" is the amount of lanes

            // i only use this for roads that need road markings, because if there are no road markings, the extra place is not needed,
            // and it can simply be w*l

            // TODO add generation of road markings

            for (Segment s : segments) {
                /*if (e.attribute != OpenStreetMap.Attributes.ISTUNNEL && !e.type.skipRoadGen()) {
                    this.placeEdge(e, world, cubeX, cubeY,cubeZ);
                }*/
                if (s.attribute != OpenStreetMap.Attributes.ISTUNNEL) {
                    s.type.fill().fill(this.gen, s, world, cubeX, cubeY, cubeZ);
                }
            }
        }
    }

    /*private void placeEdge(Segment e, World world, int cubeX, int cubeY, int cubeZ) {
        SegmentType type = e.type;
        double r = type.computeRadius(e.lanes);

        double x0 = 0;
        double b = r;
        if (Math.abs(e.slope) >= 0.000001) {
            x0 = r / Math.sqrt(1 + 1 / (e.slope * e.slope));
            b = (e.slope < 0 ? -1 : 1) * x0 * (e.slope + 1.0 / e.slope);
        }

        double minLon = e.lon0 - (cubeX * 16);
        double maxLon = e.lon1 - (cubeX * 16);
        double off = e.offset - (cubeZ * 16) + e.slope * (cubeX * 16);

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
            double ul = bound(x, e.slope, minLon, maxLon, r, x0, b, 1) + off; //TODO: save these repeated values
            double ur = bound((double) x + 1, e.slope, minLon, maxLon, r, x0, b, 1) + off;
            double ll = bound(x, e.slope, minLon, maxLon, r, x0, b, -1) + off;
            double lr = bound((double) x + 1, e.slope, minLon, maxLon, r, x0, b, -1) + off;

            double from = min(min(ul, ur), min(ll, lr));
            double to = max(max(ul, ur), max(ll, lr));

            if (!Double.isNaN(from)) {
                int ifrom = max((int) floor(from), -15);
                int ito = min((int) floor(to), 31);

                for (int z = ifrom; z <= ito; z++) {
                    //get the part of the center line i am tangent to (i hate high school algebra!!!)
                    double mainX = x;
                    if (Math.abs(e.slope) >= 0.000001d) {
                        mainX = ((double) z + (double) x / e.slope - off) / (e.slope + 1 / e.slope);
                    }

                    double mainZ = e.slope * mainX + off;

                    //get distance to closest point
                    double distance = mainX - (double) x;
                    distance *= distance;
                    double t = mainZ - (double) z;
                    distance += t * t;
                    distance = Math.sqrt(distance);

                    double[] geo = this.projection.toGeo(mainX + cubeX * 16, mainZ + cubeZ * 16);

                    int y = (int) floor(this.heights.estimateLocal(geo[0], geo[1]) - cubeY * 16);

                    if (y >= 0 && y < 16) { //if not in this range, someone else will handle it
                        BlockPos surf = new BlockPos(x + cubeX * 16, y + cubeY * 16, z + cubeZ * 16);
                        IBlockState bstate = state.selectRoadState(distance, surf);

                        if (bstate != null) {
                            world.setBlockState(surf, bstate);

                            //clear the above blocks (to a point, we don't want to be here all day)
                            IBlockState defState = Blocks.AIR.getDefaultState();
                            for (int ay = y + 1; ay < 16 * 2 && world.getBlockState(new BlockPos(x + cubeX * 16, ay + cubeY * 16, z + cubeZ * 16)) != defState; ay++) {
                                world.setBlockState(new BlockPos(x + cubeX * 16, ay + cubeY * 16, z + cubeZ * 16), defState);
                            }
                        }
                    }
                }
            }
        }
    }*/
}
