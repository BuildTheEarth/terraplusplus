package net.buildtheearth.terraminusminus.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.config.condition.AndDC;
import net.buildtheearth.terraminusminus.config.condition.DoubleCondition;
import net.buildtheearth.terraminusminus.config.condition.EqualDC;
import net.buildtheearth.terraminusminus.config.condition.GreaterThanDC;
import net.buildtheearth.terraminusminus.config.condition.LessThanDC;
import net.buildtheearth.terraminusminus.config.condition.NotDC;
import net.buildtheearth.terraminusminus.config.condition.OrDC;
import net.buildtheearth.terraminusminus.config.scalarparse.d.AddDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.DivideDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.DoubleScalarParser;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FlipXDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FlipZDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FromIntDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.MultiplyDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffAutoDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffFloatingPointDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTerrariumPngDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffIntDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.SwapAxesDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.AddISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.AndISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.FlipXISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.FlipZISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.GrayscaleExtractISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.IntScalarParser;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParseJpgISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParsePngISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParseTiffISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.RGBExtractISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.RequireOpaqueISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.SwapAxesISP;
import net.buildtheearth.terraminusminus.projection.AzimuthalEquidistantProjection;
import net.buildtheearth.terraminusminus.projection.LambertAzimuthalProjection;
import net.buildtheearth.terraminusminus.projection.EqualEarthProjection;
import net.buildtheearth.terraminusminus.projection.EquirectangularProjection;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.SinusoidalProjection;
import net.buildtheearth.terraminusminus.projection.StereographicProjection;
import net.buildtheearth.terraminusminus.projection.dymaxion.BTEDymaxionProjection;
import net.buildtheearth.terraminusminus.projection.dymaxion.ConformalDynmaxionProjection;
import net.buildtheearth.terraminusminus.projection.dymaxion.DymaxionProjection;
import net.buildtheearth.terraminusminus.projection.mercator.CenteredMercatorProjection;
import net.buildtheearth.terraminusminus.projection.mercator.TransverseMercatorProjection;
import net.buildtheearth.terraminusminus.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terraminusminus.projection.transform.ClampProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.FlipHorizontalProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.FlipVerticalProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.RotateProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terraminusminus.projection.transform.SwapAxesProjectionTransform;

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
            .put("lambert_azimuthal", LambertAzimuthalProjection.class)
            .put("azimuthal_equidistant", AzimuthalEquidistantProjection.class)
            .put("stereographic", StereographicProjection.class)
            //transformations
            .put("clamp", ClampProjectionTransform.class)
            .put("flip_horizontal", FlipHorizontalProjectionTransform.class)
            .put("flip_vertical", FlipVerticalProjectionTransform.class)
            .put("offset", OffsetProjectionTransform.class)
            .put("rotate", RotateProjectionTransform.class)
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
            //parse operators (including deprecated ParseTiffXXXXDSPs)
            .put("parse_png_terrarium", ParseTerrariumPngDSP.class)
            .put("parse_tiff", ParseTiffDSP.class)
            .put("parse_tiff_auto", ParseTiffAutoDSP.class)
            .put("parse_tiff_fp", ParseTiffFloatingPointDSP.class)
            .put("parse_tiff_int", ParseTiffIntDSP.class)
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
