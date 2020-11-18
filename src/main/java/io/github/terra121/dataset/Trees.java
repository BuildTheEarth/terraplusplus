package io.github.terra121.dataset;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.ImageProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;

public class Trees extends DoubleTiledDataset {
    public static final double BLOCK_SIZE = 16 / 100000.0;
    public static final double REGION_SIZE = BLOCK_SIZE * 256;

    public Trees() {
        super(256, TerraConfig.cacheSize, new ImageProjection(), 1.0d / BLOCK_SIZE, false);
    }

    @Override
    protected String[] urls() {
        return TerraConfig.data.trees;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("tile.lon.min", String.format("%.12f", tileX * REGION_SIZE - 180))
                .put("tile.lon.max", String.format("%.12f", (tileX + 1) * REGION_SIZE - 180))
                .put("tile.lat.min", String.format("%.12f", 90 - tileZ * REGION_SIZE))
                .put("tile.lat.max", String.format("%.12f", 90 - (tileZ + 1) * REGION_SIZE));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        new TiffImageParser().getBufferedImage(new ByteSourceInputStream(new ByteBufInputStream(data), ""), Collections.emptyMap())
                .getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];

        for (int i = 0; i < iData.length; i++) { //this loop will probably be vectorized
            out[i] = (iData[i] & 0xFF) / 100.0d;
        }
        return out;
    }
}
