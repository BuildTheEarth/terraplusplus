package io.github.terra121.dataset.impl;

import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.projection.EquirectangularProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;

import java.util.Collections;

public class Trees extends DoubleTiledDataset {
    public static final double BLOCK_SIZE = 16.0d / 100000.0d;

    public Trees() {
        super(new EquirectangularProjection(), 1.0d / BLOCK_SIZE, 256, BlendMode.LINEAR);
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return TerraConfig.data.trees;
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        new TiffImageParser().getBufferedImage(new ByteSourceInputStream(new ByteBufInputStream(data), ""), Collections.emptyMap())
                .getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];

        for (int z = 0; z < TILE_SIZE; z++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                //image tiles are reversed along Z axis
                out[(z ^ TILE_MASK) * TILE_SIZE + x] = (iData[z * TILE_SIZE + x] & 0xFF) / 100.0d;
            }
        }
        return out;
    }
}
