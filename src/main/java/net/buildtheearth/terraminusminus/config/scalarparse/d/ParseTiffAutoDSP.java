package net.buildtheearth.terraminusminus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.ImageBuilder;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffContents;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffRasterData;
import org.apache.commons.imaging.formats.tiff.TiffReader;
import org.apache.commons.imaging.formats.tiff.constants.GdalLibraryTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.photometricinterpreters.PhotometricInterpreter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.Collections;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseTiffAutoDSP implements DoubleScalarParser {
    @Override
    @SneakyThrows(ImageReadException.class)
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        TiffContents contents = new TiffReader(false)
                .readDirectories(new ByteSourceInputStream(new ByteBufInputStream(buffer), ""), true, FormatCompliance.getDefault());

        double[] dst = new double[resolution * resolution];
        TiffDirectory directory = this.parse(resolution, contents, dst);
        this.postProcess(resolution, directory, dst);
        return dst;
    }

    protected void postProcess(int resolution, @NonNull TiffDirectory directory, @NonNull double[] dst) throws ImageReadException, IOException {
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

    protected TiffDirectory parse(int resolution, @NonNull TiffContents contents, @NonNull double[] dst) throws ImageReadException, IOException {
        for (TiffDirectory directory : contents.directories) {
            if (this.parse(resolution, directory, dst)) {
                return directory;
            }
        }
        throw new IllegalArgumentException("no supported TIFF directories could be found!");
    }

    protected boolean parse(int resolution, @NonNull TiffDirectory directory, @NonNull double[] dst) throws ImageReadException, IOException {
        switch (directory.getFieldValue(TiffTagConstants.TIFF_TAG_PHOTOMETRIC_INTERPRETATION)) {
            case TiffTagConstants.PHOTOMETRIC_INTERPRETATION_VALUE_WHITE_IS_ZERO:
            case TiffTagConstants.PHOTOMETRIC_INTERPRETATION_VALUE_BLACK_IS_ZERO:
                break; //grayscale
            default:
                return false; //colored formats aren't supported lol
        }

        short[] bitsPerSample = directory.getFieldValue(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE, true);
        if (bitsPerSample.length != 1) {
            return false; //more than one channel
        }

        switch (directory.findField(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT, true).getIntValue()) {
            case TiffTagConstants.SAMPLE_FORMAT_VALUE_IEEE_FLOATING_POINT:
                return this.parseFloatingPoint(resolution, directory, dst);
            case TiffTagConstants.SAMPLE_FORMAT_VALUE_UNSIGNED_INTEGER:
            case TiffTagConstants.SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER:
                return this.parseInteger(resolution, directory, dst);
            default:
                return false;
        }
    }

    protected boolean parseFloatingPoint(int resolution, @NonNull TiffDirectory directory, @NonNull double[] dst) throws ImageReadException, IOException {
        //extract floating-point raster data
        TiffRasterData data = directory.getFloatingPointRasterData(null);
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

    protected boolean parseInteger(int resolution, @NonNull TiffDirectory directory, @NonNull double[] dst) throws ImageReadException, IOException {
        //extract raw integer raster data
        BufferedImage img = directory.getTiffImage(Collections.singletonMap(TiffConstants.PARAM_KEY_CUSTOM_PHOTOMETRIC_INTERPRETER,
                new PhotometricInterpreter(0, null, 0, 0, 0) {
                    @Override
                    public void interpretPixel(ImageBuilder imageBuilder, int[] samples, int x, int y) throws ImageReadException, IOException {
                        imageBuilder.setRGB(x, y, samples[0]);
                    }

                    @Override
                    public boolean isRaw() {
                        return true;
                    }
                }));
        int w = img.getWidth();
        int h = img.getHeight();
        checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

        //compute shift and mask
        int bitsPerSample = directory.findField(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE, true).getIntValue();
        int sampleFormat = directory.findField(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT, true).getIntValue();

        long shift, mask;
        switch (sampleFormat) {
            case TiffTagConstants.SAMPLE_FORMAT_VALUE_UNSIGNED_INTEGER:
                checkArg(positive(bitsPerSample, "bitsPerSample") < Long.SIZE - 1, "bitsPerSample (%d) must be at most %d!", bitsPerSample, Long.SIZE - 1);
                mask = Long.MAX_VALUE;
                shift = 0L;
                break;
            case TiffTagConstants.SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER:
                checkArg(positive(bitsPerSample, "bitsPerSample") < Long.SIZE, "bitsPerSample (%d) must be at most %d!", bitsPerSample, Long.SIZE);
                mask = 0xFFFFFFFFFFFFFFFFL;
                shift = Long.SIZE - bitsPerSample;
                break;
            default:
                throw new IllegalArgumentException("unsupported sample format: " + sampleFormat);
        }

        int[] src = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
        checkArg(src.length == dst.length, "data length invalid?!?");
        for (int i = 0; i < dst.length; i++) {
            dst[i] = (src[i] & mask) << shift >> shift;
        }

        return true;
    }
}
