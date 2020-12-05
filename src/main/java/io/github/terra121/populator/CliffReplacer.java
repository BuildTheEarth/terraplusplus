package io.github.terra121.populator;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.HashSet;
import java.util.Set;

//sheer cliff faces should not be grass or dirt
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CliffReplacer implements IBiomeBlockReplacer {
    public static final CliffReplacer INSTANCE = new CliffReplacer();

    private static final IBlockState STONE = Blocks.STONE.getDefaultState();

    @Override
    public IBlockState getReplacedBlock(IBlockState prev, int x, int y, int z, double dx, double dy, double dz, double density) {
        if (y > 6000 || dx * dx + dz * dz > 4.0d) {
            Block block = prev.getBlock();
            if (block == Blocks.GRASS || block == Blocks.DIRT) {
                return STONE;
            }
        }

        return prev;
    }

}
