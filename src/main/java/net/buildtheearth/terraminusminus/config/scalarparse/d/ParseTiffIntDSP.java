package net.buildtheearth.terraminusminus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseTiffIntDSP extends ParseTiffAutoDSP {
    @Override
    protected boolean parseFloatingPoint(int resolution, @NonNull TiffDirectory directory, @NonNull double[] dst) throws ImageReadException, IOException {
        return false;
    }
}
