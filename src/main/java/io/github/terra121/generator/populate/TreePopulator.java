package io.github.terra121.generator.populate;

import com.google.common.collect.ImmutableSet;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.terra121.generator.CachedChunkData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;
import java.util.Set;

import static io.github.terra121.TerraConstants.*;

public class TreePopulator implements IEarthPopulator {
    protected static final Set<Block> EXTRA_SURFACE = ImmutableSet.of(
            Blocks.SAND,
            Blocks.SANDSTONE,
            Blocks.RED_SANDSTONE,
            Blocks.CLAY,
            Blocks.HARDENED_CLAY,
            Blocks.STAINED_HARDENED_CLAY,
            Blocks.SNOW,
            Blocks.MYCELIUM);

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
        if (!data.intersectsSurface(pos.getY())) { //optimization: don't try to generate trees if the cube doesn't intersect the surface
            return;
        }

        for (int i = 0, treeCount = this.treeCount(world, random, pos, biome, data); i < treeCount; ++i) {
            this.tryPlace(world, random, pos, biome);
        }
    }

    protected int treeCount(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
        double treeCover = (Double) data.getCustom(KEY_TREE_COVER);

        //got this fun formula messing around with data on desmos, estimate of tree cover -> number
        int treeCount = 30; //max so it doesn't go to infinity (which would technically be required to guarantee full coverage, but no)
        if (treeCover < 0.95d) {
            double x = Math.pow(treeCover, 1.5d);
            treeCount = (int) ((Math.log(1.0d + x) - Math.log(1.0d - x)) * 10.0d);
        }

        //null island
        if ((pos.getX() | pos.getZ()) == 0) {
            treeCount = 10;
        }

        if (treeCount != 0 && random.nextFloat() < biome.decorator.extraTreeChance) {
            treeCount++;
        }

        return treeCount;
    }

    protected void tryPlace(World world, Random random, CubePos pos, Biome biome) {
        int xOffset = ICube.SIZE / 2 + random.nextInt(ICube.SIZE);
        int zOffset = ICube.SIZE / 2 + random.nextInt(ICube.SIZE);
        BlockPos blockPos = ((ICubicWorld) world).getSurfaceForCube(pos, xOffset, zOffset, 0, ICubicWorld.SurfaceType.OPAQUE);
        if (blockPos != null && this.canPlaceAt(world, blockPos)) {
            this.placeTree(world, random, blockPos, biome);
        }
    }

    protected boolean canPlaceAt(World world, BlockPos pos) {
        BlockPos down = pos.down();
        IBlockState state = world.getBlockState(down);

        if (state.getBlock() != Blocks.GRASS && state.getBlock() != Blocks.DIRT) {
            //plant a bit of dirt to make sure trees spawn when they are supposed to even in certain hostile environments
            if (!this.isSurfaceBlock(world, down, state)) {
                return false;
            }
            world.setBlockState(down, Blocks.GRASS.getDefaultState());
        }

        return true;
    }

    protected boolean isSurfaceBlock(World world, BlockPos pos, IBlockState state) {
        return EXTRA_SURFACE.contains(state.getBlock());
    }

    protected void placeTree(World world, Random random, BlockPos pos, Biome biome) {
        WorldGenAbstractTree generator = biome.getRandomTreeFeature(random);
        generator.setDecorationDefaults();

        if (generator.generate(world, random, pos)) {
            generator.generateSaplings(world, random, pos);
        }
    }
}
