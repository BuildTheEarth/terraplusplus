package io.github.terra121.dataset.impl;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.dataset.MultiresDataset;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.http.Disk;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.daporkchop.lib.common.util.PorkUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

public class Heights extends DoubleTiledDataset {
    public static void main(String... args) throws OutOfProjectionBoundsException {
        Http.configChanged();
        ScalarDataset dataset = constructDataset(BlendMode.LINEAR);

        int sizeX = 1024;
        int sizeZ = 1024;
        double baseX = 7;
        double baseZ = 0;
        double r = 1;
        PorkUtil.simpleDisplayImage(true, get(dataset, sizeX, sizeZ, Bounds2d.of(baseX - r, baseX + r, baseZ - r, baseZ + r), 16)
                .thenApply(data -> {
                    BufferedImage img = new BufferedImage(sizeX, sizeZ, BufferedImage.TYPE_INT_ARGB);
                    double min = Arrays.stream(data).min().getAsDouble();
                    double max = Arrays.stream(data).max().getAsDouble();

                    System.out.printf("min: %f, max: %f\n", min, max);

                    for (int x = 0; x < sizeX; x++) {
                        for (int z = 0; z < sizeZ; z++) {
                            int h = clamp(floorI((data[x * sizeZ + z] - min) * 255.0d / (max - min)), 0, 255);
                            img.setRGB(x, z, 0xFF000000 | h << 16 | h << 8 | h);
                        }
                    }

                    return img;
                })
                .join());
    }

    private static CompletableFuture<double[]> get(ScalarDataset dataset, int sizeX, int sizeZ, Bounds2d bb, int parallelization) {
        CompletableFuture[] futures = new CompletableFuture[positive(parallelization, "parallelization")];
        CompletableFuture start = CompletableFuture.completedFuture(null);
        double[] dst = new double[sizeX * sizeZ];
        for (int thread = 0; thread < parallelization; thread++) {
            int minX = (sizeX / parallelization) * thread;
            int maxX = (sizeX / parallelization) * (thread + 1);
            CompletableFuture future = start;
            for (int _z = 0; _z < sizeZ; _z++) {
                for (int _x = minX; _x < maxX; _x++) {
                    int x = _x;
                    int z = _z;
                    double lon = lerp(bb.minX(), bb.maxX(), x / (double) sizeX);
                    double lat = lerp(bb.minZ(), bb.maxZ(), z / (double) sizeZ);
                    future = future.thenCompose(unused -> {
                        try {
                            return dataset.getAsync(lon, lat).thenAccept(v -> {
                                checkState(v != null && !Double.isNaN(v), v);
                                dst[x * sizeZ + z] = v;
                            });
                        } catch (OutOfProjectionBoundsException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            futures[thread] = future;
        }
        return CompletableFuture.allOf(futures).thenApply(unused -> dst);
    }

    public static ScalarDataset constructDataset(@NonNull BlendMode blend) {
        try {
            return new MultiresDataset(new MapsProjection(), MultiresDataset.configSources("heights"), (zoom, urls) -> new Heights(zoom, urls, blend));
        } catch (IOException e) {
            throw new RuntimeException("unable to load heights dataset config", e);
        }
    }

    private final String[] urls;
    private final int zoom;

    protected Heights(int zoom, @NonNull String[] urls, @NonNull BlendMode blend) {
        super(new MapsProjection(), 1 << (zoom + 8), blend);

        this.urls = urls;
        this.zoom = zoom;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return this.urls;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        super.addProperties(tileX, tileZ, builder);

        builder.put("zoom", String.valueOf(this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        ImageIO.read(new ByteBufInputStream(data)).getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];
        for (int i = 0; i < iData.length; i++) {
            if (iData[i] >>> 24 != 0xFF) { //pixel isn't fully transparent...
                out[i] = Double.NaN;
            } else {
                out[i] = ((iData[i] & 0x00FFFFFF) - 0x800000) / 256.0d;
            }
        }

        return out;
    }
}
