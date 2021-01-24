package io.github.terra121.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
