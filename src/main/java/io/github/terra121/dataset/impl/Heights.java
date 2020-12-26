package io.github.terra121.dataset.impl;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.projection.MapsProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;

import javax.imageio.ImageIO;
import java.io.IOException;

public class Heights extends DoubleTiledDataset {
    private final int zoom;

    public Heights(int zoom, @NonNull BlendMode blendMode) {
        super(new MapsProjection(), 1 << (zoom + 8), blendMode);

        this.zoom = zoom;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return TerraConfig.data.heights;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        super.addProperties(tileX, tileZ, builder);

        builder.put("zoom", String.valueOf(this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws IOException {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        ImageIO.read(new ByteBufInputStream(data)).getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];
        for (int i = 0; i < iData.length; i++) {
            out[i] = ((iData[i] & 0x00FFFFFF) - 0x800000) / 256.0d;
        }

        return out;
    }
}
