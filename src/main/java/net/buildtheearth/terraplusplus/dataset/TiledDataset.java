package net.buildtheearth.terraplusplus.dataset;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

@RequiredArgsConstructor
@Getter
public abstract class TiledDataset<V> extends Dataset<ChunkPos, V> {
    @NonNull
    protected final GeographicProjection projection;
    protected final double tileSize;
}
