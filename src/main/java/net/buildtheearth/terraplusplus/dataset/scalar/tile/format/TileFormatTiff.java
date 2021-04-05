package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.imaging.FormatCompliance;
import org.apache.commons.imaging.ImagingException;
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
            @JsonProperty("factor") double factor,
            @JsonProperty("offset") double offset) {
        this.type = type;
        this.band = notNegative(band, "band");
        this.factor = factor;
        this.offset = offset;
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
        return out;
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

                TiffField nodataField = directory.findField(GdalLibraryTagConstants.EXIF_TAG_GDAL_NO_DATA);
                float nodata = nodataField != null ? Float.parseFloat(nodataField.getStringValue()) : Float.NaN;

                throw new UnsupportedOperationException();
            }
        };

        protected abstract void getData(@NonNull TiffDirectory directory, @NonNull double[] dst, int resolution) throws ImagingException, IOException;
    }
}
