package net.buildtheearth.terraminusminus.config.scalarparse.i;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseTiffISP implements IntScalarParser {
    @Override
    @SneakyThrows(ImageReadException.class)
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        BufferedImage image = new TiffImageParser().getBufferedImage(new ByteSourceInputStream(new ByteBufInputStream(buffer), ""), Collections.emptyMap());

        int w = image.getWidth();
        int h = image.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        return image.getRGB(0, 0, resolution, resolution, null, 0, resolution);
    }
}
