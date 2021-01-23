package io.github.terra121.dataset.osm.element.line;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import io.github.terra121.dataset.osm.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.draw.DrawFunction;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

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

            double lon0 = s.x0() - Coords.cubeToMinBlock(chunkX);
            double lon1 = s.x1() - Coords.cubeToMinBlock(chunkX);
            double lat0 = s.z0() - Coords.cubeToMinBlock(chunkZ);
            double lat1 = s.z1() - Coords.cubeToMinBlock(chunkZ);

            int minX = max((int) floor(min(lon0, lon1) - radius), 0);
            int maxX = min((int) ceil(max(lon0, lon1) + radius), 16);
            int minZ = max((int) floor(min(lat0, lat1) - radius), 0);
            int maxZ = min((int) ceil(max(lat0, lat1) + radius), 16);

            double segmentLengthSq = (lon1 - lon0) * (lon1 - lon0) + (lat1 - lat0) * (lat1 - lat0);
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double r = ((x - lon0) * (lon1 - lon0) + (z - lat0) * (lat1 - lat0)) / segmentLengthSq;
                    r = MathHelper.clamp(r, 0.0d, 1.0d);

                    double dx = MathUtil.lerp(r, lon0, lon1) - x;
                    double dz = MathUtil.lerp(r, lat0, lat1) - z;
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
