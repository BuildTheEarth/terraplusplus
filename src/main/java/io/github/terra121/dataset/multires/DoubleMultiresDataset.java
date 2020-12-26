package io.github.terra121.dataset.multires;

import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
public abstract class DoubleMultiresDataset extends MultiresDataset<double[]> implements ScalarDataset {
    public DoubleMultiresDataset(GeographicProjection proj, double tileSize) {
        super(proj, tileSize);
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        return null;
    }
}
