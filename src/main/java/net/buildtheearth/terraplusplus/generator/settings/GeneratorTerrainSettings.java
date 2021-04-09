package net.buildtheearth.terraplusplus.generator.settings;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@With
@Builder(access = AccessLevel.PROTECTED)
@Jacksonized
public class GeneratorTerrainSettings {
    public static final GeneratorTerrainSettings DEFAULT = builder().build();

    @NonNull
    @Builder.Default
    protected final IBlockState fill = Blocks.STONE.getDefaultState();

    @NonNull
    @Builder.Default
    protected final IBlockState water = Blocks.WATER.getDefaultState();

    @NonNull
    @Builder.Default
    protected final IBlockState surface = Blocks.DIRT.getDefaultState();

    @NonNull
    @Builder.Default
    protected final IBlockState top = Blocks.GRASS.getDefaultState();

    @Builder.Default
    protected final boolean useCwgReplacers = true;
}
