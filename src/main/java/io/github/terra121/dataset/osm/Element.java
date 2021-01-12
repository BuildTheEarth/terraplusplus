package io.github.terra121.dataset.osm;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.terra121.generator.cache.CachedChunkData;
import lombok.NonNull;

/**
 * A generate-able OpenStreetMap element.
 *
 * @author DaPorkchop_
 */
public interface Element extends Comparable<Element> {
    /**
     * An element that can modify the {@link CachedChunkData} for a column before it's finalized.
     *
     * @author DaPorkchop_
     */
    interface CachedData extends Element {
        void apply(@NonNull int[] data, int chunkX, int chunkZ);
    }

    /**
     * An element that can modify a {@link CubePrimer} during generation.
     *
     * @author DaPorkchop_
     */
    interface Cube extends Element {
        void apply(@NonNull CachedChunkData data, @NonNull CubePrimer primer, int cubeX, int cubeY, int cubeZ);
    }
}
