package net.buildtheearth.terraminusminus;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.buildtheearth.terraminusminus.dataset.osm.BlockStateParser;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.buildtheearth.terraminusminus.substitutes.Biome;
import net.buildtheearth.terraminusminus.util.BiomeDeserializeMixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Strings.*;

public class TerraConstants {
    public static final String LIB_NAME = "terraminusminus";
    public static final String LIB_VERSION;

    static {
        String libVersion = "unknown";
        try (InputStream inputStream = TerraConstants.class.getResourceAsStream("terraminusminus.properties")) {
            if (inputStream == null) {
                throw new IOException("Terraminusminus properties file not found");
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty("version");
            if (isNullOrEmpty(version)) {
                throw new IOException("version property is missing from terraminusminus.properties (or is empty)");
            }
            libVersion = version;
        } catch (IOException e) {
            TerraMinusMinus.LOGGER.error("Failed to load Terraminusminus properties file, version will be unknown");
            TerraMinusMinus.LOGGER.catching(e);
        }
        LIB_VERSION = libVersion;
    }

    public static final Gson GSON = new GsonBuilder()
    		.registerTypeAdapter(BlockState.class, BlockStateParser.INSTANCE)
            .create();

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .addMixIn(Biome.class, BiomeDeserializeMixin.class)
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
