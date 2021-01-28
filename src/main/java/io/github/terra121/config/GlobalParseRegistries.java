package io.github.terra121.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.terra121.config.condition.AndDC;
import io.github.terra121.config.condition.DoubleCondition;
import io.github.terra121.config.condition.EqualDC;
import io.github.terra121.config.condition.GreaterThanDC;
import io.github.terra121.config.condition.LessThanDC;
import io.github.terra121.config.condition.NotDC;
import io.github.terra121.config.condition.OrDC;
import io.github.terra121.config.scalarparse.d.AddDSP;
import io.github.terra121.config.scalarparse.d.DivideDSP;
import io.github.terra121.config.scalarparse.d.DoubleScalarParser;
import io.github.terra121.config.scalarparse.d.FromIntDSP;
import io.github.terra121.config.scalarparse.d.MultiplyDSP;
import io.github.terra121.config.scalarparse.i.AddISP;
import io.github.terra121.config.scalarparse.i.AndISP;
import io.github.terra121.config.scalarparse.i.FlipXISP;
import io.github.terra121.config.scalarparse.i.GrayscaleExtractISP;
import io.github.terra121.config.scalarparse.i.IntScalarParser;
import io.github.terra121.config.scalarparse.i.ParseJpgISP;
import io.github.terra121.config.scalarparse.i.ParsePngISP;
import io.github.terra121.config.scalarparse.i.ParseTiffISP;
import io.github.terra121.config.scalarparse.i.RGBExtractISP;
import io.github.terra121.config.scalarparse.i.RequireOpaqueISP;
import io.github.terra121.config.scalarparse.i.SwapAxesISP;
import io.github.terra121.config.scalarparse.i.FlipZISP;
import io.github.terra121.projection.mercator.CenteredMercatorProjection;
import io.github.terra121.projection.EqualEarth;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.SinusoidalProjection;
import io.github.terra121.projection.mercator.WebMercatorProjection;
import io.github.terra121.projection.mercator.TransverseMercatorProjection;
import io.github.terra121.projection.airocean.Airocean;
import io.github.terra121.projection.airocean.ConformalEstimate;
import io.github.terra121.projection.airocean.ModifiedAirocean;
import io.github.terra121.projection.transform.FlipVerticalProjectionTransform;
import io.github.terra121.projection.transform.OffsetProjectionTransform;
import io.github.terra121.projection.transform.ScaleProjectionTransform;
import io.github.terra121.projection.transform.SwapAxesProjectionTransform;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

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
            .put("equal_earth", EqualEarth.class)
            .put("airocean", Airocean.class)
            .put("conformal", ConformalEstimate.class)
            .put("bteairocean", ModifiedAirocean.class)
            //transformations
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
            .put("from_int", FromIntDSP.class)
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
}
