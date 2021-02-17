package net.buildtheearth.terraplusplus.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class SnowPopulator implements IEarthPopulator {
    public static boolean canSnow(BlockPos pos, World world, boolean air) {
        if (!air && !world.isAirBlock(pos) || !world.canSnowAt(pos, false)) {
            return false;
        }

        return pos.getY() > 5000 || world.getBiome(pos).getTemperature(pos.getY() == 0 ? pos : pos.add(0, -pos.getY(), 0)) < 0.15f;
    }

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData[] datas) {
        if (canSnow(pos.getMaxBlockPos(), world, true)) {
            for (int i = 0, cx = 0; cx < 2; cx++) {
                for (int cz = 0; cz < 2; cz++) {
                    this.populateColumn(world, pos, datas[i++], (ICube.SIZE >> 1) * (cx + 1), (ICube.SIZE >> 1) * (cz + 1));
                }
            }
        }
    }

    protected void populateColumn(World world, CubePos pos, CachedChunkData data, int x, int z) {
        if (!data.intersectsSurface(pos.getY(), 0, 1)) { //optimization: don't try to generate snow below the surface
            return;
        }

        for (int dx = 0; dx < ICube.SIZE >> 1; dx++) {
            for (int dz = 0; dz < ICube.SIZE >> 1; dz++) {
                BlockPos bpos = ((ICubicWorld) world).getSurfaceForCube(pos, x + dx, z + dz, 0, ICubicWorld.SurfaceType.BLOCKING_MOVEMENT);
                if (bpos != null && canSnow(bpos, world, false)) {
                    world.setBlockState(bpos, Blocks.SNOW_LAYER.getDefaultState());
                }
            }
        }
    }
}
