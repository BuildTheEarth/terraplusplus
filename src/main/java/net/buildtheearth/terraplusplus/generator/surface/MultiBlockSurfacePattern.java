package net.buildtheearth.terraplusplus.generator.surface;

import java.util.Random;

import net.minecraft.block.state.IBlockState;

/**
 * A basic surface pattern with blocks on top of each others
 * 
 * @author SmylerMC
 */
public class MultiBlockSurfacePattern implements ISurfacePattern {
    
    private final BakedSurfacePattern baked;
    
    public MultiBlockSurfacePattern(int offset, IBlockState... states) {
        this.baked = new BakedSurfacePattern(states, offset);
    }

    @Override
    public BakedSurfacePattern bake(int x, int surfaceY, int z, Random random) {
        return this.baked;
    }

}
