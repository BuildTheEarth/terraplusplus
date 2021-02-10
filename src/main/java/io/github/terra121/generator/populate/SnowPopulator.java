package io.github.terra121.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.EarthBiomeProvider;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class SnowPopulator implements IEarthPopulator {
    public static boolean canSnow(BlockPos pos, World world, boolean air) {
        IBlockState blockstate = world.getBlockState(pos);

        if (air || (blockstate.getBlock().isAir(blockstate, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos))) {
            //this cast could fail but this function should only be called in earth anyways
            EarthBiomeProvider ebp = (EarthBiomeProvider) world.getBiomeProvider();
            try {
                /*double[] proj = ebp.projection.toGeo(pos.getX(), pos.getZ());
                return ebp.climate.isSnow(proj[0], proj[1], pos.getY());*/
                // return alt > 5000 || this.getPoint(x, y).temperature < 0; //high elevations or freezing temperatures
                throw OutOfProjectionBoundsException.get(); //TODO
            } catch (OutOfProjectionBoundsException e) { //out of bounds, assume not snow
                return false;
            }
        }
        return false;
    }

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
        if (!data.aboveSurface(pos.getY())) { //optimization: don't try to generate snow below the surface
            //TODO: i think this should actually check for >=, not >
            return;
        }

        int baseX = Coords.cubeToMinBlock(pos.getX());
        int baseY = Coords.cubeToMinBlock(pos.getY());
        int baseZ = Coords.cubeToMinBlock(pos.getZ());

        if (canSnow(new BlockPos(baseX + 8, baseY + 8, baseZ + 8), world, true)) {
            IBlockState snow = Blocks.SNOW_LAYER.getDefaultState();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = this.quickElev(world, baseX, baseZ, baseY, baseY + 16 - 1);
                    BlockPos bpos = new BlockPos(baseX + x, y, baseZ + z);

                    if (canSnow(bpos, world, false)) {
                        world.setBlockState(bpos, snow);
                    }
                }
            }
        }
    }

    private int quickElev(World world, int x, int z, int low, int high) {
        high++;

        IBlockState defState = Blocks.AIR.getDefaultState();

        while (low < high - 1) {
            int y = low + (high - low) / 2;
            if (world.getBlockState(new BlockPos(x, y, z)) == defState) {
                high = y;
            } else {
                low = y;
            }
        }

        return low;
    }
}
