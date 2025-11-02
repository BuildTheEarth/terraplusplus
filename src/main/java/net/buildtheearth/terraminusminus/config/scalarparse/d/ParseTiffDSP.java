package net.buildtheearth.terraminusminus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.bytesource.ByteSource;
import org.apache.commons.imaging.formats.tiff.*;
import org.apache.commons.imaging.formats.tiff.constants.GdalLibraryTagConstants;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.checkArg;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.*;

/**
 * Parses integer or floating-point scalar data from single-channel TIFF images, to double arrays.
 * <br>
 * This API has diverged from Terraplusplus, which has implementation specific to integers and floating-point images
 * as a workaround for Apache commons imaging not initially supporting integer images.
 *
 * @author DaPorkchop_
 * @author Smyler
 */
@JsonDeserialize
public class ParseTiffDSP implements DoubleScalarParser {

    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        ByteSource source = ByteSource.inputStream(new ByteBufInputStream(buffer), "");
        TiffContents contents = new TiffReader(false).readDirectories(source, true, FormatCompliance.getDefault());

        double[] dst = new double[resolution * resolution];
        TiffDirectory directory = this.parse(resolution, contents, dst);
        this.postProcess(resolution, directory, dst);
        return dst;
    }

    protected void postProcess(int resolution, @NonNull TiffDirectory directory, double @NonNull [] dst) throws IOException {
        TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
        if (nodataField != null) { //nodata value is set, replace all nodata values
            double nodata = Double.parseDouble(nodataField.getStringValue());
            for (int i = 0; i < dst.length; i++) {
                if (dst[i] == nodata) {
                    dst[i] = Double.NaN;
                }
            }
        }
    }

    protected TiffDirectory parse(int resolution, @NonNull TiffContents contents, double @NonNull [] dst) throws IOException {
        for (TiffDirectory directory : contents.directories) {
            if (this.parse(resolution, directory, dst)) {
                return directory;
            }
        }
        throw new IllegalArgumentException("no supported TIFF directories could be found!");
    }

    protected boolean parse(int resolution, @NonNull TiffDirectory directory, double @NonNull [] dst) throws IOException {
        if (!this.isSupportedFormat(directory)) {
            return false;
        }

        TiffRasterData data = directory.getRasterData(null);
        int w = data.getWidth();
        int h = data.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        //extend floats to doubles
        float[] src = data.getData();
        checkArg(src.length == dst.length, "data length invalid?!?");
        for (int i = 0; i < resolution * resolution; i++) {
            dst[i] = src[i];
        }

        return true;
    }

    /**
     * Verify whether this is a supported format (single-channel grayscale, encoded as IEEE 754, integers, or signed integers).
     *
     * @param directory the TIFF directory to check
     * @return true if the directory can be safely processed, false otherwise
     *
     * @throws ImagingException if the directory is missing required fields
     */
    protected boolean isSupportedFormat(TiffDirectory directory) throws ImagingException {
        switch (directory.getFieldValue(TIFF_TAG_PHOTOMETRIC_INTERPRETATION)) {
            case PHOTOMETRIC_INTERPRETATION_VALUE_WHITE_IS_ZERO:
            case PHOTOMETRIC_INTERPRETATION_VALUE_BLACK_IS_ZERO:
                break; //grayscale
            default:
                return false; //colored formats aren't supported lol
        }
        short[] bitsPerSample = directory.getFieldValue(TIFF_TAG_BITS_PER_SAMPLE, true);
        if (bitsPerSample.length != 1) {
            return false; //more than one channel
        }
        switch (directory.findField(TIFF_TAG_SAMPLE_FORMAT, true).getIntValue()) {
            case SAMPLE_FORMAT_VALUE_UNSIGNED_INTEGER:
            case SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER:
            case SAMPLE_FORMAT_VALUE_IEEE_FLOATING_POINT:
                break;
            default:
                return false;
        }
        return true;
    }

}
