package io.github.terra121.dataset.impl;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.multires.DoubleMultiresDataset;
import io.github.terra121.dataset.multires.MultiresConfig;
import io.github.terra121.dataset.multires.WrappedUrl;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.http.Disk;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.math.PMath;
import net.daporkchop.lib.common.util.PorkUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Heights extends DoubleMultiresDataset {
    public static void main(String... args) throws OutOfProjectionBoundsException {
        Http.setMaximumConcurrentRequestsTo("https://s3.amazonaws.com/", 16);

        int size = 512;
        int shift = 0;
        Heights heights = new Heights(BlendMode.LINEAR);
        BufferedImage img = new BufferedImage(size << shift, size << shift, BufferedImage.TYPE_INT_ARGB);

        int[] arr = new int[1 << shift];

        Bounds2d bounds = Bounds2d.of(7, 9, 46, 48);
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

    @Getter
    protected MultiresConfig config;

    public Heights(@NonNull BlendMode blend) {
        super(new MapsProjection(), 1 << (10), blend);

        this.config = this.loadConfig();
    }

    protected MultiresConfig loadConfig() {
        try {
            Path configPath = Disk.configFile("heights_config.json");
            return new MultiresConfig(Files.isRegularFile(configPath)
                    ? new FileInputStream(configPath.toFile())
                    : Heights.class.getResourceAsStream("/heights_config_default.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected double[] decode(int tileX, int tileZ, int zoom, @NonNull ByteBuf data) throws IOException {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        ImageIO.read(new ByteBufInputStream(data)).getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];
        for (int i = 0; i < iData.length; i++) {
            out[i] = ((iData[i] & 0x00FFFFFF) - 0x800000) / 256.0d;
        }

        return out;
    }
}
