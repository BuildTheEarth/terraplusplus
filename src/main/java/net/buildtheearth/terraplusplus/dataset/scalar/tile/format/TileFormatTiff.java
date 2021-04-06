package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffContents;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffRasterData;
import org.apache.commons.imaging.formats.tiff.TiffReader;
import org.apache.commons.imaging.formats.tiff.constants.GdalLibraryTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * {@link TileFormat} implementation for parsing scalar data tiles from TIFF images.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public class TileFormatTiff implements TileFormat {
    protected final Type type;
    protected final int band;

    protected final double factor;
    protected final double offset;

    @JsonCreator
    public TileFormatTiff(
            @JsonProperty(value = "type", required = true) @NonNull Type type,
            @JsonProperty(value = "band", required = true) int band,
            @JsonProperty("factor") Double factor,
            @JsonProperty("offset") Double offset) {
        this.type = type;
        this.band = notNegative(band, "band");
        this.factor = fallbackIfNull(factor, 1.0d);
        this.offset = fallbackIfNull(offset, 0.0d);
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

        this.applyModelPixelScale(out, resolution, directory);
        return out;
    }

    protected void applyModelPixelScale(@NonNull double[] arr, int resolution, @NonNull TiffDirectory directory) throws ImageReadException {
        //why are geotiff docs so damn hard to find? i have no idea if this is "correct", but it *works* so idrc

        double[] modelPixelScale = directory.getFieldValue(GeoTiffTagConstants.EXIF_TAG_MODEL_PIXEL_SCALE_TAG, false);
        if (modelPixelScale == null) { //unset
            return;
        }

        if (modelPixelScale[0] > 0.0d && modelPixelScale[1] < 0.0d) {
            //no-op
        } else if (modelPixelScale[0] > 0.0d && modelPixelScale[1] > 0.0d) {
            TileFormatUtils.flipZ(arr, resolution);
        } else if (modelPixelScale[0] < 0.0d && modelPixelScale[1] < 0.0d) {
            TileFormatUtils.flipX(arr, resolution);
        } else if (modelPixelScale[0] < 0.0d && modelPixelScale[1] > 0.0d) {
            TileFormatUtils.swapAxes(arr, resolution);
        }
    }

    /**
     * The different TIFF raster types.
     *
     * @author DaPorkchop_
     */
    public enum Type {
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