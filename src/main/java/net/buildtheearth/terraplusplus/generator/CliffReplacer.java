package net.buildtheearth.terraplusplus.generator;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.replacer.IBiomeBlockReplacer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

/**
 * Prevents sheer cliff faces from being grass or dirt.
 */
public final class CliffReplacer extends IBiomeBlockReplacer {
    public static final CliffReplacer INSTANCE = new CliffReplacer();

    private static final IBlockState STONE = Blocks.STONE.getDefaultState();

    private CliffReplacer() {
        super(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    protected IBlockState getReplacedBlockImpl(IBlockState previousBlock, Biome biome, int x, int y, int z, double dx, double dy, double dz, double density) {
        if (y > 6000 || dx * dx + dz * dz > 4.0d) {
            Block block = previousBlock.getBlock();
            if (block == Blocks.GRASS || block == Blocks.DIRT) {
                return STONE;
            }
        }

        return previousBlock;
    }
}
