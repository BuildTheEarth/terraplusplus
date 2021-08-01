package net.buildtheearth.terraplusplus.dataset.vector.geometry.line;

import static java.lang.Math.abs;
import static java.lang.Math.copySign;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.daporkchop.lib.common.math.PMath.floorI;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public final class NarrowLine extends AbstractLine {
    public NarrowLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines) {
        super(id, layer, draw, lines);
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, final int chunkX, final int chunkZ, @NonNull Bounds2d bounds) {
        int minChunkBlockX = Coords.cubeToMinBlock(chunkX);
        int minChunkBlockZ = Coords.cubeToMinBlock(chunkZ);
        this.segments.forEachIntersecting(bounds, s -> {
            double x0 = s.x0();
            double x1 = s.x1();
            double z0 = s.z0();
            double z1 = s.z1();

            //slope must not be infinity, slight inaccuracy shouldn't even be noticible unless you go looking for it
            double dif = x1 - x0;
            double slope = (z1 - z0) / ((abs(dif) < 0.01d ? x1 + copySign(0.01d, dif) : x1) - x0);
            double offset = z0 - slope * x0;

            if (x0 > x1) {
                double tmp = x0;
                x0 = x1;
                x1 = tmp;
            }

            int sx = max(floorI(x0) - minChunkBlockX, 0);
            int ex = min(floorI(x1) - minChunkBlockX, 15);

            for (int x = max(sx, 0); x <= ex; x++) {
                double realx = max(x + minChunkBlockX, x0);
                double nextx = min(realx + 1.0d, x1);

                int from = floorI((slope * realx + offset)) - minChunkBlockZ;
                int to = floorI((slope * nextx + offset)) - minChunkBlockZ;

                if (from > to) {
                    int tmp = from;
                    from = to;
                    to = tmp;
                }

                to = min(to, 15);

                for (int z = max(0, from); z <= to; z++) {
                    this.draw.drawOnto(builder, x, z, 1);
                }
            }
        });
    }

    @Override
    public double minX() {
        return super.minX() - 1.0d;
    }

    @Override
    public double maxX() {
        return super.maxX() + 1.0d;
    }

    @Override
    public double minZ() {
        return super.minZ() - 1.0d;
    }

    @Override
    public double maxZ() {
        return super.maxZ() + 1.0d;
    }
}
