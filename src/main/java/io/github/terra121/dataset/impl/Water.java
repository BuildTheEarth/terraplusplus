package io.github.terra121.dataset.impl;

import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.math.BinMath;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

public class Water implements ScalarDataset {
    public static final int TILE_SHIFT = 8;
    public static final int TILE_SIZE = 1 << TILE_SHIFT;
    public static final int TILE_MASK = TILE_SIZE - 1;

    public final WaterGround grounding;
    public final OpenStreetMap osm;

    public final HashSet<ChunkPos> inverts;
    public boolean doingInverts;

    public Water(OpenStreetMap osm) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("assets/terra121/data/ground.dat");
        this.grounding = new WaterGround(is);
        this.osm = osm;
        this.inverts = new HashSet<>();
        this.doingInverts = false;
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        class State extends AbstractState<Double> {
            public State(Bounds2d paddedLocalBounds) {
                super(paddedLocalBounds);
            }

            @Override
            public Double apply(Void unused) { //stage 2: actually compute the value now that the tiles have been fetched
                return this.get(lon, lat);
            }
        }

        Bounds2d paddedLocalBounds = Bounds2d.of(lon, lon, lat, lat).expand(OpenStreetMap.TILE_SIZE / TILE_SIZE);
        return new State(paddedLocalBounds).future();
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        class State extends AbstractState<double[]> {
            protected final CornerBoundingBox2d localBounds;

            public State(@NonNull CornerBoundingBox2d localBounds, Bounds2d paddedLocalBounds) {
                super(paddedLocalBounds);

                this.localBounds = localBounds;
            }

            @Override
            public double[] apply(Void unused) { //stage 2: actually compute the values now that the tiles have been fetched
                double stepX = 1.0d / sizeX;
                double stepZ = 1.0d / sizeZ;

                double[] point = new double[2];
                double[] out = new double[sizeX * sizeZ];

                double fx = 0.0d;
                for (int i = 0, x = 0; x < sizeX; x++, fx += stepX) {
                    double fz = 0.0d;
                    for (int z = 0; z < sizeZ; z++, fz += stepZ) {
                        //compute coordinates of point
                        point = this.localBounds.point(point, fx, fz);

                        //sample value at point
                        out[i++] = this.get(point[0], point[1]);
                    }
                }

                return out;
            }
        }

        Bounds2d paddedLocalBounds = bounds.axisAlign().expand(OpenStreetMap.TILE_SIZE / TILE_SIZE);
        return new State(bounds, paddedLocalBounds).future();
    }

    @RequiredArgsConstructor
    protected abstract class AbstractState<R> implements Function<Void, R> {
        final Long2ObjectMap<OSMRegion> loadedTiles = new Long2ObjectOpenHashMap<>();

        @NonNull
        protected final Bounds2d paddedLocalBounds;

        public int getState(double lon, double lat) {
            OSMRegion region = this.loadedTiles.get(BinMath.packXY(floorI(lon / OpenStreetMap.TILE_SIZE), floorI(lat / OpenStreetMap.TILE_SIZE)));
            if (region == null) {
                return 0;
            }

            //transform to water render res
            lon -= region.west;
            lat -= region.south;
            lon /= OpenStreetMap.TILE_SIZE / TILE_SIZE;
            lat /= OpenStreetMap.TILE_SIZE / TILE_SIZE;

            int idx = region.getStateIdx((short) lon, (short) lat);

            int state = region.states[(int) lon][idx];

            if (Water.this.doingInverts && (state == 0 || state == 1) && Water.this.inverts.contains(region.coord)) {
                state = state == 1 ? 0 : 1; //invert state if in an inverted region
            }

            return state;
        }

        public double get(double lon, double lat) {
            //bound check
            if (!(lon <= 180 && lon >= -180 && lat <= 80 && lat >= -80)) {
                if (lat < -80) //antartica is land
                {
                    return 0;
                }
                return 2; //all other out of bounds is water
            }

            double oshift = OpenStreetMap.TILE_SIZE / TILE_SIZE;
            double ashift = OpenStreetMap.TILE_SIZE / TILE_SIZE;

            //rounding errors fixed by recalculating values from scratch (wonder if this glitch also causes the oddly strait terrain that sometimes appears)
            double Ob = Math.floor(lon / oshift) * oshift;
            double Ab = Math.floor(lat / ashift) * ashift;

            double Ot = Math.ceil(lon / oshift) * oshift;
            double At = Math.ceil(lat / ashift) * ashift;

            double u = (lon - Ob) / oshift;
            double v = (lat - Ab) / ashift;

            int ll = this.getState(Ob, Ab);
            int lr = this.getState(Ot, Ab);
            int ur = this.getState(Ot, At);
            int ul = this.getState(Ob, At);

            //all is ocean
            if (ll == 2 || lr == 2 || ur == 2 || ul == 2) {
                if (ll < 2) {
                    ll += 1;
                }
                if (lr < 2) {
                    lr += 1;
                }
                if (ur < 2) {
                    ur += 1;
                }
                if (ul < 2) {
                    ul += 1;
                }
            }

            //get perlin style interpolation on this block
            return (1 - v) * (ll * (1 - u) + lr * u) + (ul * (1 - u) + ur * u) * v;
        }

        public CompletableFuture<R> future() {
            ChunkPos[] tilePositions = this.paddedLocalBounds.toTiles(OpenStreetMap.TILE_SIZE);

            return CompletableFuture.allOf(Arrays.stream(tilePositions)
                    .map(pos -> Water.this.osm.getTileAsync(pos)
                            .thenApply(tile -> { //put tile directly into map when it's loaded
                                //synchronize because we can't be certain that all of the futures will be completed by the same thread
                                synchronized (this.loadedTiles) {
                                    this.loadedTiles.put(BinMath.packXY(pos.x, pos.z), tile);
                                }
                                return tile;
                            }))
                    .toArray(CompletableFuture[]::new))
                    .thenApplyAsync(this);
        }
    }
}
