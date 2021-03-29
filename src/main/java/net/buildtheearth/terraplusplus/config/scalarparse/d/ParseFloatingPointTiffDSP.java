package net.buildtheearth.terraplusplus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffContents;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffRasterData;
import org.apache.commons.imaging.formats.tiff.TiffReader;
import org.apache.commons.imaging.formats.tiff.constants.GdalLibraryTagConstants;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseFloatingPointTiffDSP implements DoubleScalarParser {
    @Override
    @SneakyThrows(ImageReadException.class)
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        TiffContents contents = new TiffReader(false)
                .readDirectories(new ByteSourceInputStream(new ByteBufInputStream(buffer), ""), true, FormatCompliance.getDefault());
        TiffDirectory directory = contents.directories.get(0);

        TiffRasterData data = directory.getFloatingPointRasterData(null);
        int w = data.getWidth();
        int h = data.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
        float nodata = nodataField != null ? Float.parseFloat(nodataField.getStringValue()) : Float.NaN;

        double[] out = new double[resolution * resolution];
        for (int i = 0; i < resolution * resolution; i++) {
            float f = data.getData()[i];
            out[i] = f == nodata ? Double.NaN : f;
        }
        return out;
    }
}
