package net.buildtheearth.terraplusplus.util;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.util.jackson.mixin.BiomeMixin;
import net.buildtheearth.terraplusplus.util.jackson.mixin.BlockStateMixin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import org.apache.sis.referencing.CommonCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

@UtilityClass
public class TerraConstants {
    public final String MODID = "terraplusplus";
    public String VERSION = "(development_snapshot)";

    public String CC_VERSION = "unknown";

    public final String CHAT_PREFIX = "&2&lT++ &8&l> ";
    public final String defaultCommandNode = MODID + ".command.";
    public final String othersCommandNode = MODID + ".others";

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
            .addMixIn(Biome.class, BiomeMixin.class)
            .addMixIn(IBlockState.class, BlockStateMixin.class)
            .build();

    /**
     * The {@link CoordinateReferenceSystem} used by Terra++ for geographic coordinates.
     */
    public static final CoordinateReferenceSystem TPP_GEO_CRS = CommonCRS.WGS84.normalizedGeographic();

    /**
     * Earth's circumference around the equator, in meters.
     */
    public final double EARTH_CIRCUMFERENCE = 40075017;

    /**
     * Earth's circumference around the poles, in meters.
     */
    public final double EARTH_POLAR_CIRCUMFERENCE = 40008000;

    public final double[] EMPTY_DOUBLE_ARRAY = new double[0];
}
