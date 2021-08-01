package net.buildtheearth.terraplusplus.dataset.vector.geometry.line;

import static java.lang.Math.abs;
import static java.lang.Math.copySign;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.daporkchop.lib.common.math.PMath.ceilI;
import static net.daporkchop.lib.common.math.PMath.floorI;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

/**
 * A line style that resembles WorldEdit //line tool, that BTE Builders seem to prefer a lot.
 * 
 * @author SmylerMC
 */
public class SharpLine extends AbstractLine {
    
    public SharpLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines) {
        super(id, layer, draw, lines);
    }

    @Override
    public void apply(@NonNull CachedChunkData.Builder builder, final int chunkX, final int chunkZ, @NonNull Bounds2d bounds) {
        final int minChunkBlockX = Coords.cubeToMinBlock(chunkX);
        final int minChunkBlockZ = Coords.cubeToMinBlock(chunkZ);
        this.segments.forEachIntersecting(bounds, s -> {
            
            // All subsequent calculation can happen in the chunk's coordinate system
            double x0 = s.x0() - minChunkBlockX;
            double x1 = s.x1() - minChunkBlockX;
            double z0 = s.z0() - minChunkBlockZ;
            double z1 = s.z1() - minChunkBlockZ;
            
            double dx = x1 - x0;
            double dz = z1 - z0;
            double absDx = abs(dx);
            double absDz = abs(dz);
            
            if(absDx >= absDz) { // Calculate Z from X (line is more horizontal than vertical)
                if(absDx < 0.01d) dx += copySign(0.01d, dx); // Avoid infinite slopes
                double slope = dz / dx;
                double offset = z0 - slope * x0;
                
                if(x0 > x1) {
                    double temp = x1;
                    x1 = x0;
                    x0 = temp;
                }
                
                double sx = max(0, x0);
                double ex = min(x1, 15.999d);
                double step = (ex - sx) / ceilI(ex - sx);
                
                for(double x = sx; x <= ex; x += step) {
                    double z = slope * x + offset;
                    if(z >= 0 && z < 16) this.draw.drawOnto(builder, floorI(x), floorI(z), 1);
                }
            } else { // Calculate X from Z (line is more vertical than horizontal)
                if(absDz < 0.01d) dz += copySign(0.01d, dz); // Avoid infinite slopes
                double slope = dx / dz;
                double offset = x0 - slope * z0;
                
                if(z0 > z1) {
                    double temp = z1;
                    z1 = z0;
                    z0 = temp;
                }
                
                double sz = max(0, z0);
                double ez = min(z1, 15.999d);
                double step = (ez - sz) / ceilI(ez - sz);
                
                for(double z = sz; z <= ez; z += step) {
                    double x = slope * z + offset;
                    if(x >= 0 && x < 16) this.draw.drawOnto(builder, floorI(x), floorI(z), 1);
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
