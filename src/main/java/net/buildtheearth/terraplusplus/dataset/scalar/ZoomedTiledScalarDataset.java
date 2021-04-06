package net.buildtheearth.terraplusplus.dataset.scalar;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.BlendMode;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormat;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileMode;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terraplusplus.util.http.Http;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link DoubleTiledDataset} which combines a {@link TileMode}, {@link TileFormat}, tile resolution, and some number of base URLs at a fixed zoom level.
 *
 * @author DaPorkchop_
 */
public class ZoomedTiledScalarDataset extends DoubleTiledDataset {
    protected static GeographicProjection scaleProjection(@NonNull GeographicProjection projection, int zoom) {
        if (notNegative(zoom, "zoom") == 0) {
            return projection;
        } else {
            return new ScaleProjectionTransform(projection, 1 << zoom, 1 << zoom);
        }
    }

    protected final String[] urls;
    protected final TileMode mode;
    protected final TileFormat format;
    protected final int zoom;

    public ZoomedTiledScalarDataset(@NonNull String[] urls, int resolution, int zoom, @NonNull TileMode mode, @NonNull TileFormat format, @NonNull GeographicProjection projection) {
        super(scaleProjection(projection, zoom), resolution, BlendMode.CUBIC);

        this.urls = urls;
        this.mode = mode;
        this.format = format;
        this.zoom = zoom;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return Http.suffixAll(this.urls, this.mode.path(tileX, tileZ, this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        return this.format.parse(data, this.resolution);
    }
}
