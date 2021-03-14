package net.buildtheearth.terraminusminus.dataset.vector.geometry.line;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static net.daporkchop.lib.common.math.PMath.floorI;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraminusminus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;
import net.buildtheearth.terraminusminus.util.MathUtils;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public final class WideLine extends AbstractLine {
    protected final double radius;

    public WideLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines, double radius) {
        super(id, layer, draw, lines);

        this.radius = radius;
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int chunkX, int chunkZ, @NonNull Bounds2d bounds) {
        this.segments.forEachIntersecting(bounds.expand(this.radius), s -> {
            double radius = this.radius;
            double radiusSq = radius * radius;

            double lon0 = s.x0() - ChunkPos.cubeToMinBlock(chunkX);
            double lon1 = s.x1() - ChunkPos.cubeToMinBlock(chunkX);
            double lat0 = s.z0() - ChunkPos.cubeToMinBlock(chunkZ);
            double lat1 = s.z1() - ChunkPos.cubeToMinBlock(chunkZ);

            int minX = max((int) floor(min(lon0, lon1) - radius), 0);
            int maxX = min((int) ceil(max(lon0, lon1) + radius), 16);
            int minZ = max((int) floor(min(lat0, lat1) - radius), 0);
            int maxZ = min((int) ceil(max(lat0, lat1) + radius), 16);

            double segmentLengthSq = (lon1 - lon0) * (lon1 - lon0) + (lat1 - lat0) * (lat1 - lat0);
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double r = ((x - lon0) * (lon1 - lon0) + (z - lat0) * (lat1 - lat0)) / segmentLengthSq;
                    r = MathUtils.clamp(r, 0.0d, 1.0d);

                    double dx = MathUtils.lerp(r, lon0, lon1) - x;
                    double dz = MathUtils.lerp(r, lat0, lat1) - z;
                    double dSq = dx * dx + dz * dz;
                    if (dSq < radiusSq) {
                        this.draw.drawOnto(builder, x, z, floorI(radius - sqrt(dSq)));
                    }
                }
            }
        });
    }

    @Override
    public double minX() {
        return super.minX() - this.radius;
    }

    @Override
    public double maxX() {
        return super.maxX() + this.radius;
    }

    @Override
    public double minZ() {
        return super.minZ() - this.radius;
    }

    @Override
    public double maxZ() {
        return super.maxZ() + this.radius;
    }
}
