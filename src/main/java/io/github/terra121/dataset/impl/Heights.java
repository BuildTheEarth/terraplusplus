package io.github.terra121.dataset.impl;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.MultiresDataset;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.http.Disk;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.math.PMath;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.util.PorkUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;

public class Heights extends DoubleTiledDataset {
    public static void main(String... args) throws OutOfProjectionBoundsException {
        Http.setMaximumConcurrentRequestsTo("https://s3.amazonaws.com/", 16);

        int size = 512;
        int shift = 0;
        ScalarDataset heights = Heights.constructDataset(BlendMode.LINEAR);
        BufferedImage img = new BufferedImage(size << shift, size << shift, BufferedImage.TYPE_INT_ARGB);

        int[] arr = new int[1 << shift];

        Bounds2d bounds = Bounds2d.of(15.1, 15.4, 46.1, 46.4);
        double[] data = heights
                .getAsync(bounds.toCornerBB(new EquirectangularProjection(), true), size, size)
                .join();

        double min = Arrays.stream(data).min().orElse(0.0d);
        double max = Arrays.stream(data).max().orElse(1.0d);
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                double d = data[x * size + z];
                int c = PMath.floorI((d - min) / (max - min) * 255.0d);
                Arrays.fill(arr, 0xFF000000 | c << 16 | c << 8 | c);
                img.setRGB(x << shift, z << shift, 1 << shift, 1 << shift, arr, 0, 0);
            }
        }

        PorkUtil.simpleDisplayImage(true, img);
    }

    public static ScalarDataset constructDataset(@NonNull BlendMode blend) {
        try {
            URL url = Heights.class.getResource("/heights_config_default.json");
            if (TerraConfig.data.customHeights) {
                File configFile = Disk.configFile("heights_config.json").toFile();
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
