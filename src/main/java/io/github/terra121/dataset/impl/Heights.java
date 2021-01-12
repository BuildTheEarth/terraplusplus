package io.github.terra121.dataset.impl;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.dataset.MultiresDataset;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.util.http.Disk;
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
