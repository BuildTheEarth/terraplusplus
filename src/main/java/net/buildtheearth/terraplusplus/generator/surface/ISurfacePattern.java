package net.buildtheearth.terraplusplus.generator.surface;

import java.util.Random;

/**
 * A vertical pattern that should be drawn at the surface of the ground
 * 
 * @author SmylerMC
 */
public interface ISurfacePattern {
    
    BakedSurfacePattern bake(int x, int surfaceY, int z, Random random);

}
