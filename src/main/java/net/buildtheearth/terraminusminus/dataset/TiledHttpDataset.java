package net.buildtheearth.terraminusminus.dataset;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.util.http.Http;
import net.daporkchop.lib.common.misc.string.PStrings;

/**
 * @author DaPorkchop_
 */
public abstract class TiledHttpDataset<T> extends TiledDataset<T> {
    public TiledHttpDataset(@NonNull GeographicProjection projection, double tileSize) {
        super(projection, tileSize);
    }

    protected abstract String[] urls(int tileX, int tileZ);

    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("lon.min", PStrings.fastFormat("%.12f", tileX * this.tileSize))
                .put("lon.max", PStrings.fastFormat("%.12f", (tileX + 1) * this.tileSize))
                .put("lat.min", PStrings.fastFormat("%.12f", tileZ * this.tileSize))
                .put("lat.max", PStrings.fastFormat("%.12f", (tileZ + 1) * this.tileSize));
    }

    protected abstract T decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception;

    @Override
    public CompletableFuture<T> load(@NonNull ChunkPos pos) throws Exception {
        String[] urls = this.urls(pos.x(), pos.z());

        if (urls == null || urls.length == 0) { //no urls for tile
            return CompletableFuture.completedFuture(null);
        }

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        this.addProperties(pos.x(), pos.z(), builder);
        Map<String, String> properties = builder.build();

        return Http.getFirst(
                Arrays.stream(urls).map(url -> Http.formatUrl(properties, url)).toArray(String[]::new),
                Http.RequestOptions.builder().build(),
                data -> this.decode(pos.x(), pos.z(), data));
    }
}
