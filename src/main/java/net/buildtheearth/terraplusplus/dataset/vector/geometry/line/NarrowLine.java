package net.buildtheearth.terraplusplus.dataset.vector.geometry.line;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public class NarrowLine extends AbstractLine {
    protected final int weight;

    public NarrowLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines, int weight) {
        super(id, layer, draw, lines);

        this.weight = weight;
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, int tileX, int tileZ, int zoom, @NonNull Bounds2d bounds) {
        int baseX = Coords.cubeToMinBlock(tileX);
        int baseZ = Coords.cubeToMinBlock(tileZ);
        double scale = 1.0d / (1 << zoom);

        this.segments.forEachIntersecting(bounds, s -> {
            double x0 = s.x0() * scale;
            double x1 = s.x1() * scale;
            double z0 = s.z0() * scale;
            double z1 = s.z1() * scale;

            //slope must not be infinity, slight inaccuracy shouldn't even be noticeable unless you go looking for it
            double dif = x1 - x0;
            double slope = (z1 - z0) / ((abs(dif) < 0.01d * scale ? x1 + copySign(0.01d * scale, dif) : x1) - x0);
            double offset = z0 - slope * x0;

            if (x0 > x1) {
                double tmp = x0;
                x0 = x1;
                x1 = tmp;
            }

            int sx = max(floorI(x0) - baseX, 0);
            int ex = min(floorI(x1) - baseX, 15);

            for (int x = max(sx, 0); x <= ex; x++) {
                double realx = max(x + baseX, x0);
                double nextx = min(realx + 1.0d, x1);

                int from = floorI((slope * realx + offset)) - baseZ;
                int to = floorI((slope * nextx + offset)) - baseZ;

                if (from > to) {
                    int tmp = from;
                    from = to;
                    to = tmp;
                }

                to = min(to, 15);

                for (int z = max(0, from); z <= to; z++) {
                    this.draw.drawOnto(builder, x, z, this.weight);
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
