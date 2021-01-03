package io.github.terra121.dataset.osm.poly;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * Polygon block filling modes.
 *
 * @author DaPorkchop_
 */
public enum PolygonFill {
    SURFACE {
        @Override
        public void fill(double[] heights, CubePrimer primer, OSMPolygon p, int cubeX, int cubeY, int cubeZ) {
            IBlockState state = Blocks.WATER.getDefaultState();

            int baseX = Coords.cubeToMinBlock(cubeX);
            int baseY = Coords.cubeToMinBlock(cubeY);
            int baseZ = Coords.cubeToMinBlock(cubeZ);

            p.rasterizeShape(baseX, 16, baseZ, 16, (blockX, blockZ) -> {
                int x = blockX - baseX;
                int z = blockZ - baseZ;
                int y = floorI(heights[x * 16 + z]) - baseY;
                if ((y & 0xF) != y) { //if not in this range, someone else will handle it
                    return;
                }

                primer.setBlockState(x, y, z, state);
            });
        }
    };

    public abstract void fill(double[] heights, CubePrimer primer, OSMPolygon p, int cubeX, int cubeY, int cubeZ);
}
