package io.github.terra121.generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.DeserializationException;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import io.github.terra121.TerraConstants;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.transform.OffsetProjectionTransform;
import io.github.terra121.projection.transform.ScaleProjectionTransform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.ref.Ref;
import net.minecraft.world.biome.BiomeProvider;

import java.io.IOException;
import java.io.InputStream;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

@JsonDeserialize
public class EarthGeneratorSettings {
    public static final int CONFIG_VERSION = 2;

    public static final String DEFAULT_SETTINGS;

    static {
        try (InputStream in = EarthGeneratorSettings.class.getResourceAsStream("default_generator_settings.json5")) {
            DEFAULT_SETTINGS = new String(StreamUtil.toByteArray(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EarthGeneratorSettings parse(String generatorSettings) {
        if (Strings.isNullOrEmpty(generatorSettings)) {
            generatorSettings = DEFAULT_SETTINGS;
        }

        try {
            if (!generatorSettings.contains("version")) { //upgrade legacy config
                LegacyConfig legacy = JSON_MAPPER.readValue(generatorSettings, LegacyConfig.class);

                GeographicProjection projection = JSON_MAPPER.readValue("{\"" + legacy.projection + "\":{}}",GeographicProjection.class);
                projection = GeographicProjection.orientProjectionLegacy(projection, legacy.orentation);
                projection = new ScaleProjectionTransform(projection, legacy.scaleX, legacy.scaleY);
                projection = new OffsetProjectionTransform(projection, legacy.offsetX, legacy.offsetY);

                return new EarthGeneratorSettings(projection, legacy.customcubic, CONFIG_VERSION);
            }

            return JSON_MAPPER.readValue(generatorSettings, EarthGeneratorSettings.class);
        } catch (IOException e) {
            throw new RuntimeException("unable to parse generator settings!", e);
        }
    }

    @Deprecated
    public JsonSettings settings;

    @Getter
    protected final GeographicProjection projection;
    @Getter(AccessLevel.PRIVATE)
    protected final String cwg;

    protected final Ref<BiomeProvider> biomeProvider = Ref.soft(() -> new EarthBiomeProvider(this.projection()));

    protected final Ref<CustomGeneratorSettings> customCubic = Ref.soft(() -> {
        CustomGeneratorSettings cfg;
        if (Strings.isNullOrEmpty(this.cwg())) { //use new minimal defaults
            cfg = new CustomGeneratorSettings();
            cfg.mineshafts = cfg.caves = cfg.strongholds = cfg.dungeons = cfg.ravines = false;
            cfg.lakes.clear();
        } else {
            try {
                cfg = CustomGenSettingsSerialization.jankson().fromJsonCarefully(this.cwg(), CustomGeneratorSettings.class);
            } catch (PresetLoadError | DeserializationException err) {
                throw new RuntimeException(err);
            } catch (SyntaxError err) {
                String message = err.getMessage() + '\n' + err.getLineMessage();
                throw new RuntimeException(message, err);
            }
        }
        cfg.waterLevel = 0;
        return cfg;
    });

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EarthGeneratorSettings(
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
            @JsonProperty("cwg") String cwg,
            @JsonProperty(value = "version", required = true) int version) {
        checkState(version == CONFIG_VERSION, "invalid version %d (expected: %d)", version, CONFIG_VERSION);

        this.projection = projection.optimize();
        this.cwg = cwg;
    }

    @Override
    public String toString() {
        return TerraConstants.GSON.toJson(this.settings, JsonSettings.class);
    }

    public BiomeProvider biomeProvider() {
        return this.biomeProvider.get();
    }

    public CustomGeneratorSettings customCubic() {
        return this.customCubic.get();
    }

    public GeographicProjection getNormalizedProjection() {
        /*return GeographicProjection.orientProjection(
                GeographicProjection.projections.get(this.settings.projection), GeographicProjection.Orientation.upright);*/
        //TODO: this
        return null;
    }

    //json template to be filled by Gson

    //what moron made this a separate inner class?
    // - DaPorkchop_, 2021
    public static class JsonSettings {
        public String projection = "equirectangular";
        public GeographicProjection.Orientation orentation = GeographicProjection.Orientation.none; // This typo is unfortunate, but let's keep it for backward compatibility
        public double scaleX = 100000.0d;
        public double scaleY = 100000.0d;
        public double offsetX = 0;
        public double offsetY = 0;
        public boolean smoothblend = true;
        public boolean roads = true;
        public String customcubic = "";
        public boolean dynamicbaseheight = true;
        @Deprecated
        public boolean osmwater = true;
        public boolean buildings = true;
    }

    @JsonDeserialize
    private static class LegacyConfig {
        public String projection = "equirectangular";
        public GeographicProjection.Orientation orentation = GeographicProjection.Orientation.none;
        public double scaleX = 100000.0d;
        public double scaleY = 100000.0d;
        public double offsetX = 0;
        public double offsetY = 0;
        public boolean smoothblend = true;
        public boolean roads = true;
        public String customcubic = "";
        public boolean dynamicbaseheight = true;
        public boolean osmwater = true;
        public boolean buildings = true;
    }
}