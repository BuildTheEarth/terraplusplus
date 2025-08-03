package net.buildtheearth.terraplusplus.config.scalarparse.d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.TerraMod;


/**
 * Parses scalar data from TIFF files.
 *
 * @deprecated floating point and integer specific TIFF implementations were made irrelevant
 * by Apache Commons imaging supporting them both ina unified way.
 * Use {@link ParseTiffDSP} instead, this class may be removed in future releases.
 */
@JsonDeserialize
@Deprecated
public class ParseTiffAutoDSP extends ParseTiffDSP {

    public ParseTiffAutoDSP() {
        TerraMod.LOGGER.warn("parse_tiff_auto and ParseTiffAutoDSP are deprecated. Use parse_tiff and ParseTiffDSP instead");
    }

}
