package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.util.bvh.BVH;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

@RequiredArgsConstructor
@EqualsAndHashCode
public final class OSMRegion {
    @NonNull
    public final ChunkPos coord;
    @NonNull
    @EqualsAndHashCode.Exclude
    public final BVH<VectorGeometry> elements;
}
