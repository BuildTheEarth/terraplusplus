package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageBuilder;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffContents;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffRasterData;
import org.apache.commons.imaging.formats.tiff.TiffReader;
import org.apache.commons.imaging.formats.tiff.constants.GdalLibraryTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.photometricinterpreters.PhotometricInterpreter;
import sun.awt.image.IntegerComponentRaster;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;

import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * {@link TileFormat} implementation for parsing scalar data tiles from TIFF images.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public class TileFormatTiff implements TileFormat {
    private static final double[] MODEL_PIXEL_SCALE_DEFAULT = { 1.0d, 1.0d };

    protected final Type type;
    protected final int band;

    protected final double factor;
    protected final double offset;

    protected final TileTransform transform;

    @JsonCreator
    public TileFormatTiff(
            @JsonProperty(value = "type", required = true) @NonNull Type type,
            @JsonProperty(value = "band", required = true) int band,
            @JsonProperty("factor") Double factor,
            @JsonProperty("offset") Double offset,
            @JsonProperty(value = "transform", required = true) @NonNull TileTransform transform) {
        this.type = type;
        this.band = notNegative(band, "band");
        this.factor = fallbackIfNull(factor, 1.0d);
        this.offset = fallbackIfNull(offset, 0.0d);
        this.transform = transform;
    }

    @Override
    @SneakyThrows({ ImagingException.class, IOException.class })
    public double[] parse(@NonNull ByteBuf buf, int resolution) {
        TiffContents contents = new TiffReader(false)
                .readDirectories(new ByteSourceInputStream(new ByteBufInputStream(buf), ""), true, FormatCompliance.getDefault());
        TiffDirectory directory = contents.directories.get(this.band);

        double[] out = new double[resolution * resolution];
        this.type.getData(directory, out, resolution);

        for (int i = 0; i < out.length; i++) { //scale and offset values (this will likely be auto-vectorized)
            out[i] = out[i] * this.factor + this.offset;
        }

        this.transform.process(out, resolution);
        return out;
    }

    /**
     * The different TIFF raster types.
     *
     * @author DaPorkchop_
     */
    public enum Type {
        Byte {
            @Override
            protected void getData(@NonNull TiffDirectory directory, @NonNull double[] dst, int resolution) throws ImagingException, IOException {
                BufferedImage img = directory.getTiffImage();
                int w = img.getWidth();
                int h = img.getHeight();
                checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

                TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
                int nodata = nodataField != null ? Integer.parseInt(nodataField.getStringValue()) : -1;

                int[] data = ((IntegerComponentRaster) img.getRaster()).getDataStorage();
                checkArg(data.length == dst.length, "data length invalid?!?");

                for (int i = 0; i < dst.length; i++) {
                    int v = data[i] & 0xFF;
                    dst[i] = v != nodata ? v : Double.NaN;
                }
            }
        },
        Int16 {
            @Override
            protected void getData(@NonNull TiffDirectory directory, @NonNull double[] dst, int resolution) throws ImagingException, IOException {
                BufferedImage img = directory.getTiffImage(Collections.singletonMap(TiffConstants.PARAM_KEY_CUSTOM_PHOTOMETRIC_INTERPRETER,
                        new PhotometricInterpreter(0, null, 0, 0, 0) {
                            @Override
                            public void interpretPixel(ImageBuilder imageBuilder, int[] samples, int x, int y) throws ImageReadException, IOException {
                                imageBuilder.setRGB(x, y, (short) samples[0]);
                            }

                            @Override
                            public boolean isRaw() {
                                return true;
                            }
                        }));
                int w = img.getWidth();
                int h = img.getHeight();
                checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);

                TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
                int nodata = nodataField != null ? Integer.parseInt(nodataField.getStringValue()) : -1;

                int[] data = ((IntegerComponentRaster) img.getRaster()).getDataStorage();
                checkArg(data.length == dst.length, "data length invalid?!?");

                for (int i = 0; i < dst.length; i++) {
                    int v = data[i];
                    dst[i] = v != nodata ? v : Double.NaN;
                }
            }
        },
        Float32 {
            @Override
            protected void getData(@NonNull TiffDirectory directory, @NonNull double[] dst, int resolution) throws ImagingException, IOException {
                TiffRasterData data = directory.getFloatingPointRasterData(null);
                int w = data.getWidth();
                int h = data.getHeight();
                checkArg(w == resolution && h == resolution, "invalid image resolution: %dx%d (expected: %dx%3$d)", w, h, resolution);
                checkArg(data.getData().length == dst.length, "data length invalid?!?");

                TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
                float nodata = nodataField != null ? Float.parseFloat(nodataField.getStringValue()) : Float.NaN;

                for (int i = 0; i < dst.length; i++) {
                    float v = data.getData()[i];
                    dst[i] = v != nodata ? v : Double.NaN;
                }
            }
        };

        protected abstract void getData(@NonNull TiffDirectory directory, @NonNull double[] dst, int resolution) throws ImagingException, IOException;
    }
}
