package net.buildtheearth.terraplusplus.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValue;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValueBinaryOperator;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValueConstant;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValueTag;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperAll;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperAny;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperCondition;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperFirst;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperNarrow;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperNothing;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapperWide;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperAll;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperAny;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperCondition;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperConvertToLines;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperDistance;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperFill;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperFirst;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapperNothing;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionAnd;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionId;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionIntersects;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionNot;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionOr;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchConditionTag;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormat;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTerrariumPng;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTiff;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileMode;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileModeSimple;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileModeSlippyMap;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionAll;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionBlock;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionNoTrees;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionOcean;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionWater;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionWeightAdd;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionWeightClamp;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionWeightGreaterThan;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunctionWeightLessThan;
import net.buildtheearth.terraplusplus.generator.biome.BiomeFilterConstant;
import net.buildtheearth.terraplusplus.generator.biome.BiomeFilterTerra121;
import net.buildtheearth.terraplusplus.generator.biome.BiomeFilterUserOverride;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraplusplus.generator.data.DataBakerOSM;
import net.buildtheearth.terraplusplus.generator.data.DataBakerHeights;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.generator.data.DataBakerInitialBiomes;
import net.buildtheearth.terraplusplus.generator.data.DataBakerNullIsland;
import net.buildtheearth.terraplusplus.generator.data.DataBakerTreeCover;
import net.buildtheearth.terraplusplus.generator.populate.PopulatorBiomeDecoration;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.buildtheearth.terraplusplus.generator.populate.PopulatorSnow;
import net.buildtheearth.terraplusplus.generator.populate.PopulatorTrees;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettings;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettingsAll;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettingsCustom;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettingsDefault;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettingsDisable;
import net.buildtheearth.terraplusplus.generator.settings.osm.GeneratorOSMSettingsToggle;
import net.buildtheearth.terraplusplus.projection.EqualEarthProjection;
import net.buildtheearth.terraplusplus.projection.EquirectangularProjection;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.SinusoidalProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.BTEDymaxionProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.ConformalDynmaxionProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.DymaxionProjection;
import net.buildtheearth.terraplusplus.projection.mercator.CenteredMercatorProjection;
import net.buildtheearth.terraplusplus.projection.mercator.TransverseMercatorProjection;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terraplusplus.projection.transform.FlipHorizontalProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.FlipVerticalProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.SwapAxesProjectionTransform;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Identifies implementation classes by their type names.
 * <p>
 * Warning: modifying any of the fields in this class outside of initialization time may cause unexpected behavior.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class GlobalParseRegistries {
    public final BiMap<String, Class<? extends GeographicProjection>> PROJECTIONS = new BiMapBuilder<String, Class<? extends GeographicProjection>>()
            //normal projections
            .put("centered_mercator", CenteredMercatorProjection.class)
            .put("web_mercator", WebMercatorProjection.class)
            .put("transverse_mercator", TransverseMercatorProjection.class)
            .put("equirectangular", EquirectangularProjection.class)
            .put("sinusoidal", SinusoidalProjection.class)
            .put("equal_earth", EqualEarthProjection.class)
            .put("bte_conformal_dymaxion", BTEDymaxionProjection.class)
            .put("dymaxion", DymaxionProjection.class)
            .put("conformal_dymaxion", ConformalDynmaxionProjection.class)
            //transformations
            .put("flip_horizontal", FlipHorizontalProjectionTransform.class)
            .put("flip_vertical", FlipVerticalProjectionTransform.class)
            .put("offset", OffsetProjectionTransform.class)
            .put("scale", ScaleProjectionTransform.class)
            .put("swap_axes", SwapAxesProjectionTransform.class)
            .build();

    public final BiMap<String, Class<? extends TileFormat>> TILE_FORMATS = new BiMapBuilder<String, Class<? extends TileFormat>>()
            .put("terrarium_png", TileFormatTerrariumPng.class)
            .put("tiff", TileFormatTiff.class)
            .build();

    public final BiMap<String, Class<? extends TileMode>> TILE_MODES = new BiMapBuilder<String, Class<? extends TileMode>>()
            .put("simple", TileModeSimple.class)
            .put("slippy", TileModeSlippyMap.class)
            .build();

    public final BiMap<String, Class<? extends LineMapper>> OSM_LINE_MAPPERS = new BiMapBuilder<String, Class<? extends LineMapper>>()
            //mergers
            .put("all", LineMapperAll.class)
            .put("any", LineMapperAny.class)
            .put("first", LineMapperFirst.class)
            //misc.
            .put("condition", LineMapperCondition.class)
            .put("nothing", LineMapperNothing.class)
            //emitters
            .put("narrow", LineMapperNarrow.class)
            .put("wide", LineMapperWide.class)
            .build();

    public final BiMap<String, Class<? extends PolygonMapper>> OSM_POLYGON_MAPPERS = new BiMapBuilder<String, Class<? extends PolygonMapper>>()
            //mergers
            .put("all", PolygonMapperAll.class)
            .put("any", PolygonMapperAny.class)
            .put("first", PolygonMapperFirst.class)
            //misc.
            .put("condition", PolygonMapperCondition.class)
            .put("convert_to_lines", PolygonMapperConvertToLines.class)
            .put("nothing", PolygonMapperNothing.class)
            //emitters
            .put("rasterize_distance", PolygonMapperDistance.class)
            .put("rasterize_fill", PolygonMapperFill.class)
            .build();

    public final BiMap<String, Class<? extends DValue>> OSM_DVALUES = new BiMapBuilder<String, Class<? extends DValue>>()
            //math operators
            .put("+", DValueBinaryOperator.Add.class)
            .put("-", DValueBinaryOperator.Subtract.class)
            .put("*", DValueBinaryOperator.Multiply.class)
            .put("/", DValueBinaryOperator.Divide.class)
            .put("floor_div", DValueBinaryOperator.FloorDiv.class)
            .put("min", DValueBinaryOperator.Min.class)
            .put("max", DValueBinaryOperator.Max.class)
            //misc.
            .put("constant", DValueConstant.class)
            .put("tag", DValueTag.class)
            .build();

    public final BiMap<String, Class<? extends MatchCondition>> OSM_MATCH_CONDITIONS = new BiMapBuilder<String, Class<? extends MatchCondition>>()
            //logical operations
            .put("and", MatchConditionAnd.class)
            .put("not", MatchConditionNot.class)
            .put("or", MatchConditionOr.class)
            //misc.
            .put("id", MatchConditionId.class)
            .put("intersects", MatchConditionIntersects.class)
            .put("tag", MatchConditionTag.class)
            .build();

    public final BiMap<String, Class<? extends DrawFunction>> VECTOR_DRAW_FUNCTIONS = new BiMapBuilder<String, Class<? extends DrawFunction>>()
            //mergers
            .put("all", DrawFunctionAll.class)
            //weight modifiers
            .put("weight_add", DrawFunctionWeightAdd.class)
            .put("weight_clamp", DrawFunctionWeightClamp.class)
            //weight conditions
            .put("weight_greater_than", DrawFunctionWeightGreaterThan.class)
            .put("weight_less_than", DrawFunctionWeightLessThan.class)
            //misc.
            .put("block", DrawFunctionBlock.class)
            .put("no_trees", DrawFunctionNoTrees.class)
            .put("ocean", DrawFunctionOcean.class)
            .put("water", DrawFunctionWater.class)
            .build();

    public final BiMap<String, Class<? extends GeneratorOSMSettings>> GENERATOR_SETTINGS_OSM = new BiMapBuilder<String, Class<? extends GeneratorOSMSettings>>()
            .put("all", GeneratorOSMSettingsAll.class)
            .put("custom", GeneratorOSMSettingsCustom.class)
            .put("default", GeneratorOSMSettingsDefault.class)
            .put("disable", GeneratorOSMSettingsDisable.class)
            .put("toggle", GeneratorOSMSettingsToggle.class)
            .build();

    public final BiMap<String, Class<? extends IEarthBiomeFilter>> GENERATOR_SETTINGS_BIOME_FILTER = new BiMapBuilder<String, Class<? extends IEarthBiomeFilter>>()
            .put("constant", BiomeFilterConstant.class)
            .put("legacy_terra121", BiomeFilterTerra121.class)
            .put("user_overrides", BiomeFilterUserOverride.class)
            .build();

    public final BiMap<String, Class<? extends IEarthDataBaker>> GENERATOR_SETTINGS_DATA_BAKER = new BiMapBuilder<String, Class<? extends IEarthDataBaker>>()
            .put("heights", DataBakerHeights.class)
            .put("initial_biomes", DataBakerInitialBiomes.class)
            .put("null_island", DataBakerNullIsland.class)
            .put("osm", DataBakerOSM.class)
            .put("tree_cover", DataBakerTreeCover.class)
            .build();

    public final BiMap<String, Class<? extends IEarthPopulator>> GENERATOR_SETTINGS_POPULATOR = new BiMapBuilder<String, Class<? extends IEarthPopulator>>()
            .put("biome_decorate", PopulatorBiomeDecoration.class)
            .put("snow", PopulatorSnow.class)
            .put("trees", PopulatorTrees.class)
            .build();

    /**
     * Stupid builder class so that I can populate the initial values cleanly using chained method calls.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    private static class BiMapBuilder<K, V> {
        @NonNull
        private final BiMap<K, V> delegate;

        public BiMapBuilder() {
            this(HashBiMap.create());
        }

        public BiMapBuilder<K, V> put(K key, V value) {
            this.delegate.put(key, value);
            return this;
        }

        public BiMap<K, V> build() {
            return this.delegate;
        }
    }

    /**
     * Base implementation of a {@link com.fasterxml.jackson.databind.jsontype.TypeIdResolver} which uses a {@link BiMap}.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    public static abstract class TypeIdResolver<T> extends TypeIdResolverBase {
        @NonNull
        protected final BiMap<String, Class<? extends T>> registry;

        @Override
        public String idFromValue(Object value) {
            return this.idFromValueAndType(value, value.getClass());
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return this.registry.inverse().get(suggestedType);
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            Class<? extends T> clazz = this.registry.get(id);
            checkArg(clazz != null, "unknown id: %s", id);
            return context.getConfig().getTypeFactory().constructType(clazz);
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }
}
