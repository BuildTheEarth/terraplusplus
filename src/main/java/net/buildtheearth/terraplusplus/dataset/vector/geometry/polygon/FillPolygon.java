package net.buildtheearth.terraplusplus.dataset.vector.geometry.polygon;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.jackson.IntRange;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public final class FillPolygon extends AbstractPolygon {
    public FillPolygon(@NonNull String id, double layer, @NonNull DrawFunction draw, IntRange levels, @NonNull MultiPolygon polygons) {
        super(id, layer, draw, levels, polygons);
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int tileX, int tileZ, int zoom, @NonNull Bounds2d bounds) {
        if (!this.containsZoom(zoom)) {
            return;
        }

        int baseX = Coords.cubeToMinBlock(tileX << zoom);
        int baseZ = Coords.cubeToMinBlock(tileZ << zoom);

        for (int x = 0; x < 16; x++) {
            double[] intersectionPoints = this.getIntersectionPoints((x << zoom) + baseX);

            for (int i = 0; i < intersectionPoints.length; ) {
                int min = clamp((floorI(intersectionPoints[i++]) - baseZ) >> zoom, 0, 16);
                int max = clamp((floorI(intersectionPoints[i++]) - baseZ) >> zoom, 0, 16);
                for (int z = min; z < max; z++) {
                    this.draw.drawOnto(builder, x, z, 1);
                }
            }
        }
    }
}
