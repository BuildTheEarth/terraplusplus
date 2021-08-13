package net.buildtheearth.terraplusplus.generator.surface;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;

/**
 * A surface pattern ready to be generated
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class BakedSurfacePattern {
    
    private final IBlockState[] pattern;
    private final int offset;

}
