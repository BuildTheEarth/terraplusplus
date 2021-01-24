package io.github.terra121.config;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.projection.CenteredMapsProjection;
import io.github.terra121.projection.EqualEarth;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.SinusoidalProjection;
import io.github.terra121.projection.TransverseMercatorProjection;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Identifies implementation classes by their type names.
 * <p>
 * Warning: modifying any of the fields in this class outside of initialization time may cause unexpected behavior.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class GlobalParseRegistries {
    public final Map<String, Class<? extends GeographicProjection>> PROJECTIONS = new MapBuilder<String, Class<? extends GeographicProjection>>()
            //normal projections
            .put("web_mercator", CenteredMapsProjection.class)
            .put("equirectangular", EquirectangularProjection.class)
            .put("sinusoidal", SinusoidalProjection.class)
            .put("equal_earth", EqualEarth.class)
            .put("airocean", Airocean.class)
            .put("transverse_mercator", TransverseMercatorProjection.class)
            .put("conformal", ConformalEstimate.class)
            .put("bteairocean", ModifiedAirocean.class)
            //transformations
            .put("flip_vertical", FlipVerticalProjectionTransform.class)
            .put("offset", OffsetProjectionTransform.class)
            .put("scale", ScaleProjectionTransform.class)
            .put("swap_axes", SwapAxesProjectionTransform.class)
            .build();

    /**
     * Stupid builder class so that I can populate the initial values cleanly using chained method calls.
     * <p>
     * Effectively the same as {@link ImmutableMap.Builder}, but without the immutable part.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    private static class MapBuilder<K, V> {
        @NonNull
        private final Map<K, V> delegate;

        public MapBuilder() {
            this(new HashMap<>());
        }

        public MapBuilder<K, V> put(K key, V value) {
            this.delegate.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return this.delegate;
        }
    }
}
