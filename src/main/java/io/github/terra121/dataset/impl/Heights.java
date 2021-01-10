package io.github.terra121.dataset.impl;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
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
import net.daporkchop.lib.common.misc.file.PFiles;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Heights extends DoubleTiledDataset {
    public static ScalarDataset constructDataset(@NonNull BlendMode blend) {
        try {
            URL url = Heights.class.getResource("/heights_config_default.json5");
            if (TerraConfig.data.customHeights) {
                File configFile = Disk.configFile("heights_config.json5").toFile();
                if (!PFiles.checkFileExists(configFile)) { //config file doesn't exist, create default one
                    try (InputStream in = url.openStream();
                         OutputStream out = new FileOutputStream(PFiles.ensureFileExists(configFile))) {
                        out.write(StreamUtil.toByteArray(in));
                    }
                }
                url = configFile.toURI().toURL();
            }
            return new MultiresDataset(new MapsProjection(), url, (zoom, urls) -> new Heights(zoom, urls, blend));
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
