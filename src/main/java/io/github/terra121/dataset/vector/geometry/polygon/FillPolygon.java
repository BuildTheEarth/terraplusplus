package io.github.terra121.dataset.vector.geometry.polygon;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.vector.draw.DrawFunction;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public final class FillPolygon extends AbstractPolygon {
    public FillPolygon(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiPolygon polygons) {
        super(id, layer, draw, polygons);
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int chunkX, int chunkZ, @NonNull Bounds2d bounds) {
        int baseX = Coords.cubeToMinBlock(chunkX);
        int baseZ = Coords.cubeToMinBlock(chunkZ);

        for (int x = 0; x < 16; x++) {
            double[] intersectionPoints = this.getIntersectionPoints(x + baseX);

            for (int i = 0; i < intersectionPoints.length; ) {
                int min = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, 16);
                int max = clamp(floorI(intersectionPoints[i++]) - baseZ, 0, 16);
                for (int z = min; z < max; z++) {
                    this.draw.drawOnto(builder, x, z, 1);
                }
            }
        }
    }
}
