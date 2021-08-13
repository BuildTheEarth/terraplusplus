package net.buildtheearth.terraplusplus.generator.surface;

import java.util.Random;

import net.minecraft.block.state.IBlockState;

/**
 * @author SmylerMC
 */
public class BlockSurfacePattern implements ISurfacePattern {
    
    private final IBlockState[] blocks;
    
    public BlockSurfacePattern(IBlockState block) {
        this.blocks = new IBlockState[] { block };
    }
    
    @Override
    public BakedSurfacePattern bake(int x, int surfaceY, int z, Random random) {
        return new BakedSurfacePattern(this.blocks, 0);
    }
    
    public IBlockState block() {
        return this.blocks[0];
    }

}
