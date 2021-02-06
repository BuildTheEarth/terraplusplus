package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.util.bvh.BVH;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class OSMRegion {
    @NonNull
    @EqualsAndHashCode.Include
    public final ChunkPos coord;
    @NonNull
    public final BVH<VectorGeometry> elements;
}
