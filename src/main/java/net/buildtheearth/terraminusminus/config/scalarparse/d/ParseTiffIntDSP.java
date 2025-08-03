package net.buildtheearth.terraminusminus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraminusminus.TerraMinusMinus;

/**
 * Parses int scalar data from TIFF files.
 *
 * @deprecated floating point and integer specific TIFF implementations were made irrelevant
 * by Apache commons imaging supporting them both ina unified way.
 * Use {@link ParseTiffDSP} instead, this class may be removed in future releases.
 */
@JsonDeserialize
@Deprecated
public class ParseTiffIntDSP extends ParseTiffAutoDSP {

    public ParseTiffIntDSP() {
        TerraMinusMinus.LOGGER.warn("parse_tiff_int and ParseTiffIntDSP are deprecated. Use parse_tiff and ParseTiffDSP instead");
    }

}
