package net.buildtheearth.terraplusplus.generator.populate;

import com.google.common.collect.ImmutableSet;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.data.TreeCoverBaker;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;
import java.util.Set;

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

    protected static final Cached<byte[]> RNG_CACHE = Cached.threadLocal(() -> new byte[(ICube.SIZE >> 1) * (ICube.SIZE >> 1)], ReferenceStrength.SOFT);

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData[] datas) {
        byte[] rng = RNG_CACHE.get();

        for (int i = 0, cx = 0; cx < 2; cx++) {
            for (int cz = 0; cz < 2; cz++) {
                this.populateColumn(world, random, pos, biome, datas[i++], (ICube.SIZE >> 1) * (cx + 1), (ICube.SIZE >> 1) * (cz + 1), rng);
            }
        }
    }

    protected void populateColumn(World world, Random random, CubePos pos, Biome biome, CachedChunkData data, int x, int z, byte[] rng) {
        if (!data.intersectsSurface(pos.getY())) { //optimization: don't try to generate snow below the surface
            return;
        }

        byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);
        random.nextBytes(rng);

        for (int i = 0, dx = 0; dx < ICube.SIZE >> 1; dx++) {
            for (int dz = 0; dz < ICube.SIZE >> 1; dz++, i++) {
                if ((rng[i] & 0xFF) < (treeCover[(((x + dx) & 0xF) << 4) | ((z + dz) & 0xF)] & 0xFF)) {
                    this.tryPlace(world, random, pos, biome, x + dx, z + dz);
                }
            }
        }
    }

    protected void tryPlace(World world, Random random, CubePos pos, Biome biome, int x, int z) {
        BlockPos blockPos = ((ICubicWorld) world).getSurfaceForCube(pos, x, z, 0, ICubicWorld.SurfaceType.OPAQUE);
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
