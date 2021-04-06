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
import net.buildtheearth.terraplusplus.config.condition.AndDC;
import net.buildtheearth.terraplusplus.config.condition.DoubleCondition;
import net.buildtheearth.terraplusplus.config.condition.EqualDC;
import net.buildtheearth.terraplusplus.config.condition.GreaterThanDC;
import net.buildtheearth.terraplusplus.config.condition.LessThanDC;
import net.buildtheearth.terraplusplus.config.condition.NotDC;
import net.buildtheearth.terraplusplus.config.condition.OrDC;
import net.buildtheearth.terraplusplus.config.scalarparse.d.AddDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.DivideDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.DoubleScalarParser;
import net.buildtheearth.terraplusplus.config.scalarparse.d.FlipXDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.FlipZDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.FromIntDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.MultiplyDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.ParseFloatingPointTiffDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.ParseTerrariumPngDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.d.SwapAxesDSP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.AddISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.AndISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.FlipXISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.FlipZISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.GrayscaleExtractISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.IntScalarParser;
import net.buildtheearth.terraplusplus.config.scalarparse.i.ParseJpgISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.ParsePngISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.ParseTiffISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.RGBExtractISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.RequireOpaqueISP;
import net.buildtheearth.terraplusplus.config.scalarparse.i.SwapAxesISP;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormat;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTiff;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileMode;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileModeSimple;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileModeSlippyMap;
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

    public final BiMap<String, Class<? extends DoubleCondition>> DOUBLE_CONDITIONS = new BiMapBuilder<String, Class<? extends DoubleCondition>>()
            //conditions
            .put("equal", EqualDC.class)
            .put("greater_than", GreaterThanDC.class)
            .put("less_than", LessThanDC.class)
            //logical operators
            .put("and", AndDC.class)
            .put("not", NotDC.class)
            .put("or", OrDC.class)
            .build();

    public final BiMap<String, Class<? extends DoubleScalarParser>> SCALAR_PARSERS_DOUBLE = new BiMapBuilder<String, Class<? extends DoubleScalarParser>>()
            //arithmetic operators
            .put("add", AddDSP.class)
            .put("divide", DivideDSP.class)
            .put("multiply", MultiplyDSP.class)
            //conversion operators
            .put("flip_x", FlipXDSP.class)
            .put("flip_z", FlipZDSP.class)
            .put("from_int", FromIntDSP.class)
            .put("swap_axes", SwapAxesDSP.class)
            //parse operators
            .put("parse_png_terrarium", ParseTerrariumPngDSP.class)
            .put("parse_tiff_fp", ParseFloatingPointTiffDSP.class)
            .build();

    public final BiMap<String, Class<? extends IntScalarParser>> SCALAR_PARSERS_INT = new BiMapBuilder<String, Class<? extends IntScalarParser>>()
            //arithmetic operators
            .put("add", AddISP.class)
            //logical operators
            .put("and", AndISP.class)
            //conversion operators
            .put("flip_x", FlipXISP.class)
            .put("flip_z", FlipZISP.class)
            .put("grayscale_extract", GrayscaleExtractISP.class)
            .put("require_opaque", RequireOpaqueISP.class)
            .put("rgb_extract", RGBExtractISP.class)
            .put("swap_axes", SwapAxesISP.class)
            //parse operators
            .put("parse_jpg", ParseJpgISP.class)
            .put("parse_png", ParsePngISP.class)
            .put("parse_tiff", ParseTiffISP.class)
            .build();

    public final BiMap<String, Class<? extends TileFormat>> TILE_FORMATS = new BiMapBuilder<String, Class<? extends TileFormat>>()
            .put("tiff", TileFormatTiff.class)
            .build();

    public final BiMap<String, Class<? extends TileMode>> TILE_MODES = new BiMapBuilder<String, Class<? extends TileMode>>()
            .put("simple", TileModeSimple.class)
            .put("slippy", TileModeSlippyMap.class)
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
