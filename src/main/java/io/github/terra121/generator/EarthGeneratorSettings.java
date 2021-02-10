package io.github.terra121.generator;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.JsonGrammar;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.DeserializationException;
import io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.api.SyntaxError;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.PresetLoadError;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.transform.FlipVerticalProjectionTransform;
import io.github.terra121.projection.transform.OffsetProjectionTransform;
import io.github.terra121.projection.transform.ScaleProjectionTransform;
import io.github.terra121.projection.transform.SwapAxesProjectionTransform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.ref.Ref;
import net.minecraft.world.biome.BiomeProvider;

import java.io.IOException;
import java.io.InputStream;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(onMethod_ = { @JsonGetter })
@With
public class EarthGeneratorSettings {
    public static final int CONFIG_VERSION = 2;

    private static final LoadingCache<String, EarthGeneratorSettings> SETTINGS_PARSE_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues()
            .build(CacheLoader.from(EarthGeneratorSettings::parseUncached));

    public static final String DEFAULT_SETTINGS;
    public static final String BTE_DEFAULT_SETTINGS;

    static {
        try {
            try (InputStream in = EarthGeneratorSettings.class.getResourceAsStream("default_generator_settings.json5")) {
                DEFAULT_SETTINGS = new String(StreamUtil.toByteArray(in));
            }
            try (InputStream in = EarthGeneratorSettings.class.getResourceAsStream("bte_generator_settings.json5")) {
                BTE_DEFAULT_SETTINGS = new String(StreamUtil.toByteArray(in));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the given generator settings.
     *
     * @param generatorSettings the settings string to parse
     * @return the parsed settings
     */
    public static EarthGeneratorSettings parse(String generatorSettings) {
        if (Strings.isNullOrEmpty(generatorSettings)) {
            generatorSettings = DEFAULT_SETTINGS;
        }

        return SETTINGS_PARSE_CACHE.getUnchecked(generatorSettings);
    }

    /**
     * Parses the given generator settings without caching.
     *
     * @param generatorSettings the settings string to parse
     * @return the parsed settings
     */
    @SneakyThrows(IOException.class)
    public static EarthGeneratorSettings parseUncached(String generatorSettings) {
        if (Strings.isNullOrEmpty(generatorSettings)) {
            generatorSettings = DEFAULT_SETTINGS;
        }

        if (!generatorSettings.contains("version")) { //upgrade legacy config
            TerraMod.LOGGER.info("Parsing legacy config: {}", generatorSettings);

            LegacyConfig legacy = JSON_MAPPER.readValue(generatorSettings, LegacyConfig.class);

            GeographicProjection projection = JSON_MAPPER.readValue("{\"" + LegacyConfig.upgradeLegacyProjectionName(legacy.projection) + "\":{}}", GeographicProjection.class);
            projection = LegacyConfig.orientProjectionLegacy(projection, legacy.orentation);
            if (legacy.scaleX != 1.0d || legacy.scaleY != 1.0d) {
                projection = new ScaleProjectionTransform(projection, legacy.scaleX, legacy.scaleY);
            }
            if (legacy.offsetX != 0.0d || legacy.offsetY != 0.0d) {
                projection = new OffsetProjectionTransform(projection, legacy.offsetX, legacy.offsetY);
            }

            return new EarthGeneratorSettings(projection, legacy.customcubic, true, true, CONFIG_VERSION);
        }

        return JSON_MAPPER.readValue(generatorSettings, EarthGeneratorSettings.class);
    }

    @NonNull
    protected final GeographicProjection projection;
    @NonNull
    protected final String cwg;

    protected final boolean useDefaultHeights;
    protected final boolean useDefaultTreeCover;

    @Getter(AccessLevel.NONE)
    protected transient final Ref<BiomeProvider> biomeProvider = Ref.soft(() -> new EarthBiomeProvider(this));
    @Getter(AccessLevel.NONE)
    protected transient final Ref<CustomGeneratorSettings> customCubic = Ref.soft(() -> {
        CustomGeneratorSettings cfg;
        if (this.cwg().isEmpty()) { //use new minimal defaults
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
    @Getter(AccessLevel.NONE)
    protected transient final Ref<GeneratorDatasets> datasets = Ref.soft(() -> new GeneratorDatasets(this));

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EarthGeneratorSettings(
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
            @JsonProperty(value = "cwg") String cwg,
            @JsonProperty(value = "useDefaultHeights") Boolean useDefaultHeights,
            @JsonProperty(value = "useDefaultTreeCover") @JsonAlias("useDefaultTrees") Boolean useDefaultTreeCover,
            @JsonProperty(value = "version", required = true) int version) {
        checkState(version == CONFIG_VERSION, "invalid version %d (expected: %d)", version, CONFIG_VERSION);

        this.projection = projection;
        this.cwg = Strings.isNullOrEmpty(cwg) ? "" : CustomGeneratorSettingsFixer.INSTANCE.fixJson(cwg).toJson(JsonGrammar.COMPACT);
        this.useDefaultHeights = useDefaultHeights != null ? useDefaultHeights : true;
        this.useDefaultTreeCover = useDefaultTreeCover != null ? useDefaultTreeCover : true;
    }

    @Override
    public String toString() {
        try {
            return JSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonGetter("version")
    private int getVersion() {
        return CONFIG_VERSION;
    }

    public BiomeProvider biomeProvider() {
        return this.biomeProvider.get();
    }

    public CustomGeneratorSettings customCubic() {
        return this.customCubic.get();
    }

    public GeneratorDatasets datasets() {
        return this.datasets.get();
    }

    @JsonDeserialize
    private static class LegacyConfig {
        private static GeographicProjection orientProjectionLegacy(GeographicProjection base, Orientation orientation) {
            if (base.upright()) {
                if (orientation == Orientation.upright) {
                    return base;
                }
                base = new FlipVerticalProjectionTransform(base);
            }

            if (orientation == Orientation.swapped) {
                return new SwapAxesProjectionTransform(base);
            } else if (orientation == Orientation.upright) {
                base = new FlipVerticalProjectionTransform(base);
            }

            return base;
        }

        private static String upgradeLegacyProjectionName(String name) {
            switch (name) {
                case "web_mercator":
                    return "centered_mercator";
                case "airocean":
                    return "dymaxion";
                case "conformal":
                    return "conformal_dymaxion";
                case "bteairocean":
                    return "bte_conformal_dymaxion";
                default:
                    return name;
            }
        }

        public String projection = "equirectangular";
        public Orientation orentation = Orientation.none;
        public double scaleX = 100000.0d;
        public double scaleY = 100000.0d;
        public double offsetX;
        public double offsetY;
        public boolean smoothblend = true;
        public boolean roads = true;
        public String customcubic = "";
        public boolean dynamicbaseheight = true;
        public boolean osmwater = true;
        public boolean buildings = true;

        private enum Orientation {
            none, upright, swapped
        }
    }
}