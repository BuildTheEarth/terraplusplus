package io.github.terra121;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.terra121.dataset.osm.config.BlockStateParser;
import io.github.terra121.util.BlockStateDeserializeMixin;
import net.minecraft.block.state.IBlockState;

public class TerraConstants {
    public static final String prefix = "&2&lT++ &8&l> ";
    public static final String modID = "terra121";

    public static final String defaultCommandNode = modID + ".command.";
    public static final String controlCommandNode = modID + ".commands.";
    public static final String adminCommandNode = modID + ".admin";

    public static final String version = "1.0";

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(IBlockState.class, BlockStateParser.INSTANCE)
            .create();

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .addMixIn(IBlockState.class, BlockStateDeserializeMixin.class)
            .build();

    /**
     * Earth's circumference around the equator, in meters.
     */
    public static final double EARTH_CIRCUMFERENCE = 40075017;

    /**
     * Earth's circumference around the poles, in meters.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 40008000;

    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
}
