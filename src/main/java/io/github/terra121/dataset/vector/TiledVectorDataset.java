package io.github.terra121.dataset.vector;

import io.github.terra121.dataset.IVectorDataset;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.projection.GeographicProjection;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
public abstract class TiledVectorDataset extends TiledDataset<VectorGeometry[]> implements IVectorDataset {
    public TiledVectorDataset(@NonNull GeographicProjection projection, double tileSize) {
        super(projection, tileSize);
    }
}
