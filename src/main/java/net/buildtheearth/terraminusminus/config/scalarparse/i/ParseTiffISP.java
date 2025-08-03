package net.buildtheearth.terraminusminus.config.scalarparse.i;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImagingParameters;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseTiffISP implements IntScalarParser {
    @Override
    @SneakyThrows(ImageReadException.class)
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        ByteSource source = new ByteSourceInputStream(new ByteBufInputStream(buffer), "");
        BufferedImage image = new TiffImageParser().getBufferedImage(source, new TiffImagingParameters());

        int w = image.getWidth();
        int h = image.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        return image.getRGB(0, 0, resolution, resolution, null, 0, resolution);
    }
}
