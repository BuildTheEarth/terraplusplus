package net.buildtheearth.terraplusplus.dataset.geojson.dataset;

import net.buildtheearth.terraplusplus.dataset.IDataset;
import net.buildtheearth.terraplusplus.dataset.IElementDataset;
import net.buildtheearth.terraplusplus.dataset.TiledDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraplusplus.projection.EquirectangularProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public class TiledGeoJsonDataset extends TiledDataset<GeoJsonObject[]> implements IElementDataset<GeoJsonObject[]> {
    protected final IDataset<String, GeoJsonObject[]> delegate;

    public TiledGeoJsonDataset(@NonNull IDataset<String, GeoJsonObject[]> delegate) {
        super(new EquirectangularProjection(), 1.0d / 64.0d);

        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<GeoJsonObject[]> load(@NonNull ChunkPos key) throws Exception {
        return this.delegate.getAsync(String.format("tile/%d/%d.json", key.x, key.z));
    }

    @Override
    public CompletableFuture<GeoJsonObject[][]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        Bounds2d localBounds = bounds.fromGeo(this.projection).axisAlign();
        CompletableFuture<GeoJsonObject[]>[] futures = uncheckedCast(Arrays.stream(localBounds.toTiles(this.tileSize))
                .map(this::getAsync)
                .toArray(CompletableFuture[]::new));

        return CompletableFuture.allOf(futures).thenApply(unused ->
                Arrays.stream(futures)
                        .map(CompletableFuture::join)
                        .toArray(GeoJsonObject[][]::new));
    }
}
