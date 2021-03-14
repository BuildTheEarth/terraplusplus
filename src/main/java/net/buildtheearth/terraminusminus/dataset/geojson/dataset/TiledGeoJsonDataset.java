package net.buildtheearth.terraminusminus.dataset.geojson.dataset;

import static net.daporkchop.lib.common.util.PorkUtil.uncheckedCast;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.IDataset;
import net.buildtheearth.terraminusminus.dataset.IElementDataset;
import net.buildtheearth.terraminusminus.dataset.TiledDataset;
import net.buildtheearth.terraminusminus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraminusminus.projection.EquirectangularProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

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
