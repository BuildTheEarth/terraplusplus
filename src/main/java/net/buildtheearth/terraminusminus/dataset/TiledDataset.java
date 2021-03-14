package net.buildtheearth.terraminusminus.dataset;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;

@RequiredArgsConstructor
@Getter
public abstract class TiledDataset<V> extends Dataset<ChunkPos, V> {
    @NonNull
    protected final GeographicProjection projection;
    protected final double tileSize;
}
