package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class TileFormatTerrariumPng implements TileFormat {
    @Override
    @SneakyThrows(IOException.class)
    public double[] parse(@NonNull ByteBuf buf, int resolution) {
        int[] arr = ImageIO.read(new ByteBufInputStream(buf)).getRGB(0, 0, resolution, resolution, null, 0, resolution);

        double[] out = new double[resolution * resolution];
        for (int i = 0; i < resolution * resolution; i++) {
            int c = arr[i];
            if ((c >>> 24) == 0xFF) {
                out[i] = ((c & 0x00FFFFFF) * (1.0d / 256.0d)) - 32768.0d;
            } else { //pixel isn't opaque
                out[i] = Double.NaN;
            }
        }

        TileTransform.FLIP_Y.process(out, resolution);

        return out;
    }
}
