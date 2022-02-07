package net.buildtheearth.terraminusminus.dataset.vector.geometry.polygon;

import static net.daporkchop.lib.common.math.PMath.clamp;
import static net.daporkchop.lib.common.math.PMath.floorI;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraminusminus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public final class FillPolygon extends AbstractPolygon {
    public FillPolygon(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiPolygon polygons) {
        super(id, layer, draw, polygons);
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int chunkX, int chunkZ, @NonNull Bounds2d bounds) {
        int baseX = ChunkPos.cubeToMinBlock(chunkX);
        int baseZ = ChunkPos.cubeToMinBlock(chunkZ);

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
