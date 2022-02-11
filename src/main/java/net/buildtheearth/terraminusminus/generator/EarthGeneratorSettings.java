package net.buildtheearth.terraminusminus.generator;

import static net.daporkchop.lib.common.util.PValidation.checkState;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.TerraMinusMinus;
import net.buildtheearth.terraminusminus.config.GlobalParseRegistries;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.transform.FlipHorizontalProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.FlipVerticalProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.ProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.SwapAxesProjectionTransform;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class EarthGeneratorSettings {
    public static final int CONFIG_VERSION = 2;
    public static final String DEFAULT_SETTINGS;
    private static final LoadingCache<String, EarthGeneratorSettings> SETTINGS_PARSE_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues()
            .build(CacheLoader.from(EarthGeneratorSettings::parseUncached));
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
            TerraMinusMinus.LOGGER.info("Parsing legacy config: {}", generatorSettings);

            LegacyConfig legacy = TerraConstants.JSON_MAPPER.readValue(generatorSettings, LegacyConfig.class);

            GeographicProjection projection = TerraConstants.JSON_MAPPER.readValue("{\"" + LegacyConfig.upgradeLegacyProjectionName(legacy.projection) + "\":{}}", GeographicProjection.class);
            projection = LegacyConfig.orientProjectionLegacy(projection, legacy.orentation);
            if (legacy.scaleX != 1.0d || legacy.scaleY != 1.0d) {
                projection = new ScaleProjectionTransform(projection, legacy.scaleX, legacy.scaleY);
            }

            return new EarthGeneratorSettings(projection, legacy.customcubic, true, true, CONFIG_VERSION);
        }

        return TerraConstants.JSON_MAPPER.readValue(generatorSettings, EarthGeneratorSettings.class);
    }

    @NonNull
    @Getter(onMethod_ = { @JsonGetter })
    protected final GeographicProjection projection;

    @Getter(onMethod_ = { @JsonGetter })
    protected final boolean useDefaultHeights;
    @Getter(onMethod_ = { @JsonGetter })
    protected final boolean useDefaultTreeCover;

    protected transient final Cached<EarthBiomeProvider> biomeProvider = Cached.global(() -> new EarthBiomeProvider(this), ReferenceStrength.SOFT);
    protected transient final Cached<GeneratorDatasets> datasets = Cached.global(() -> new GeneratorDatasets(this), ReferenceStrength.SOFT);

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EarthGeneratorSettings(
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
            @JsonProperty(value = "cwg") String cwg,
            @JsonProperty(value = "useDefaultHeights") Boolean useDefaultHeights,
            @JsonProperty(value = "useDefaultTreeCover") @JsonAlias("useDefaultTrees") Boolean useDefaultTreeCover,
            @JsonProperty(value = "version", required = true) int version) {
        checkState(version == CONFIG_VERSION, "invalid version %d (expected: %d)", version, CONFIG_VERSION);

        this.projection = projection;
        this.useDefaultHeights = useDefaultHeights != null ? useDefaultHeights : true;
        this.useDefaultTreeCover = useDefaultTreeCover != null ? useDefaultTreeCover : true;
    }

    @Override
    public String toString() {
        try {
            return TerraConstants.JSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonGetter("version")
    private int version() {
        return CONFIG_VERSION;
    }

    public EarthBiomeProvider biomeProvider() {
        return this.biomeProvider.get();
    }

    public GeneratorDatasets datasets() {
        return this.datasets.get();
    }

    /**
     * Tries to convert this generator settings to a String Terra121 could understand.
     *
     * @return a valid String representing this settings for Terra121, or null if not possible (e.g. using Terra++ features like offsets)
     * @author SmylerMC
     */
    @JsonIgnore
    public String getLegacyGeneratorString() {
        LegacyConfig legacy = new LegacyConfig();
        legacy.scaleX = 1;
        legacy.scaleY = 1;
        GeographicProjection proj = this.projection();
        GeographicProjection base = proj;
        ProjectionTransform transform = null;

        while (base instanceof ProjectionTransform) {
            base = ((ProjectionTransform) base).delegate();
        }

        legacy.projection = LegacyConfig.downgradeToLegacyProjectionName(GlobalParseRegistries.PROJECTIONS.inverse().get(base.getClass()));
        if (legacy.projection == null) {
            return null;
        }

        while (proj instanceof ProjectionTransform) {
            ProjectionTransform trs = (ProjectionTransform) proj;
            if (proj instanceof ScaleProjectionTransform) {
                ScaleProjectionTransform scale = (ScaleProjectionTransform) proj;
                legacy.scaleX *= scale.x();
                legacy.scaleY *= scale.y();
            } else if (proj instanceof FlipVerticalProjectionTransform || proj instanceof SwapAxesProjectionTransform) {
                if (transform != null) {
                    return null; // Terra121 does not support multiple transformations
                }
                transform = trs;
            } else if (proj instanceof FlipHorizontalProjectionTransform || proj instanceof OffsetProjectionTransform) {
                return null; // Terra121 does not support horizontal flips and offsets
            }
            proj = trs.delegate();
        }

        if (base.upright()) {
            if (transform == null) {
                legacy.orentation = LegacyConfig.Orientation.upright;
            } else if (transform instanceof FlipVerticalProjectionTransform) {
                legacy.orentation = LegacyConfig.Orientation.none; // Terra121 will flip it anyway because it's upright
            } else if (transform instanceof SwapAxesProjectionTransform) {
                return null; // There is one case we are not handling here, that of an upright, swapped and flipped vertically projection
            }
        } else {
            if (transform == null) {
                legacy.orentation = LegacyConfig.Orientation.none;
            } else if (transform instanceof FlipVerticalProjectionTransform) {
                legacy.orentation = LegacyConfig.Orientation.upright;
            } else if (transform instanceof SwapAxesProjectionTransform) {
                legacy.orentation = LegacyConfig.Orientation.swapped;
            }
        }
        try {
            return TerraConstants.JSON_MAPPER.writeValueAsString(legacy);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @JsonDeserialize
    @JsonSerialize
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

        private static String downgradeToLegacyProjectionName(String name) {
            switch (name) {
                case "centered_mercator":
                    return "web_mercator";
                case "dymaxion":
                    return "airocean";
                case "conformal_dymaxion":
                    return "conformal";
                case "bte_conformal_dymaxion":
                    return "bteairocean";
                case "equirectangular":
                case "sinusoidal":
                case "equal_earth":
                case "transverse_mercator":
                    return name;
                default:
                    return null;
            }
        }

        public String projection = "equirectangular";
        public Orientation orentation = Orientation.none;
        public double scaleX = 100000.0d;
        public double scaleY = 100000.0d;
        public String customcubic = "";

        @JsonAnySetter
        private void fallback(String key, String value) {
            TerraMinusMinus.LOGGER.warn("Ignoring unknown legacy config option: \"{}\": \"{}\"", key, value);
        }

        private enum Orientation {
            none, upright, swapped
        }
    }
}