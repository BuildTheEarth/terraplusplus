package net.buildtheearth.terraplusplus;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.buildtheearth.terraplusplus.dataset.osm.BlockStateParser;
import net.buildtheearth.terraplusplus.util.BiomeDeserializeMixin;
import net.buildtheearth.terraplusplus.util.BlockStateDeserializeMixin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

public class TerraConstants {
    public static final String MODID = "terraplusplus";
    public static String VERSION = "(development_snapshot)";

    public static String CC_VERSION = "unknown";

    public static final String CHAT_PREFIX = "&2&lT++ &8&l> ";
    public static final String defaultCommandNode = MODID + ".command.";
    public static final String othersCommandNode = MODID + ".others";

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(IBlockState.class, BlockStateParser.INSTANCE)
            .create();

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .addMixIn(Biome.class, BiomeDeserializeMixin.class)
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
