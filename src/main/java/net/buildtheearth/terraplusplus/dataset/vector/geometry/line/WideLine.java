package net.buildtheearth.terraplusplus.dataset.vector.geometry.line;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
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
    public void apply(@NonNull CachedChunkData.Builder builder, int tileX, int tileZ, int zoom, @NonNull Bounds2d bounds) {
        int baseX = Coords.cubeToMinBlock(tileX << zoom);
        int baseZ = Coords.cubeToMinBlock(tileZ << zoom);
        int step = 1 << zoom;

        this.segments.forEachIntersecting(bounds.expand(this.radius), s -> {
            double radius = this.radius;
            double radiusSq = radius * radius;

            double lon0 = s.x0() - baseX;
            double lon1 = s.x1() - baseX;
            double lat0 = s.z0() - baseZ;
            double lat1 = s.z1() - baseZ;

            int minX = max((int) floor(min(lon0, lon1) - radius), 0);
            int maxX = min((int) ceil(max(lon0, lon1) + radius), 16 << zoom);
            int minZ = max((int) floor(min(lat0, lat1) - radius), 0);
            int maxZ = min((int) ceil(max(lat0, lat1) + radius), 16 << zoom);

            double segmentLengthSq = (lon1 - lon0) * (lon1 - lon0) + (lat1 - lat0) * (lat1 - lat0);
            for (int x = minX; x < maxX; x += step) {
                for (int z = minZ; z < maxZ; z += step) {
                    double r = ((x - lon0) * (lon1 - lon0) + (z - lat0) * (lat1 - lat0)) / segmentLengthSq;
                    r = MathHelper.clamp(r, 0.0d, 1.0d);

                    double dx = MathUtil.lerp(r, lon0, lon1) - x;
                    double dz = MathUtil.lerp(r, lat0, lat1) - z;
                    double dSq = dx * dx + dz * dz;
                    if (dSq < radiusSq) {
                        this.draw.drawOnto(builder, x >> zoom, z >> zoom, floorI(radius - sqrt(dSq)));
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
