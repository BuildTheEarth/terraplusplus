package io.github.terra121.generator.populate;

import com.google.common.collect.ImmutableSet;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.data.TreeCoverBaker;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
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

    protected static final Ref<byte[]> RNG_CACHE = ThreadRef.soft(() -> new byte[16 * 16]);

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
        if (!data.intersectsSurface(pos.getY())) { //optimization: don't try to generate trees if the cube doesn't intersect the surface
            return;
        }

        byte[] treeCover = (byte[]) data.getCustom(KEY_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);

        byte[] rng = RNG_CACHE.get();
        random.nextBytes(rng);

        for (int i = 0, x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++, i++) {
                if ((rng[i] & 0xFF) < (treeCover[i] & 0xFF)) {
                    this.tryPlace(world, random, pos, biome, x, z);
                }
            }
        }
    }

    protected void tryPlace(World world, Random random, CubePos pos, Biome biome, int x, int z) {
        BlockPos blockPos = ((ICubicWorld) world).getSurfaceForCube(pos, x + 8, z + 8, 0, ICubicWorld.SurfaceType.OPAQUE);
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
