package net.buildtheearth.terraplusplus.config.scalarparse.i;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class ParseJpgISP extends ParsePngISP {
    //we don't actually need to do anything different, since ImageIO does automatic type detection...
}
