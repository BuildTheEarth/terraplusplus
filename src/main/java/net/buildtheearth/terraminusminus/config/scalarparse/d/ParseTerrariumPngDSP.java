package net.buildtheearth.terraminusminus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.config.scalarparse.i.IntScalarParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseTerrariumPngDSP implements DoubleScalarParser {
    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        BufferedImage image = ImageIO.read(new ByteBufInputStream(buffer));

        int w = image.getWidth();
        int h = image.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        int[] rgb = image.getRGB(0, 0, resolution, resolution, null, 0, resolution);
        double[] out = new double[resolution * resolution];

        for (int i = 0; i < resolution * resolution; i++) {
            int c = rgb[i];
            if ((c >>> 24) != 0xFF) { //nodata
                out[i] = Double.NaN;
            } else {
                out[i] = ((c & ~0xFF000000) - 0x00800000) * (1.0d / 256.0d);
            }
        }

        return out;
    }
}
