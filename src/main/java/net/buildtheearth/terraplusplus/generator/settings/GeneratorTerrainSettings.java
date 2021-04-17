package net.buildtheearth.terraplusplus.generator.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.function.Supplier;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@With
public class GeneratorTerrainSettings {
    public static final GeneratorTerrainSettings DEFAULT = null;

    @NonNull
    protected final Supplier<IBlockState> fill;

    @NonNull
    protected final Supplier<IBlockState> water;

    @NonNull
    protected final Supplier<IBlockState> surface;

    @NonNull
    protected final Supplier<IBlockState> top;

    protected final boolean useCwgReplacers;

    public GeneratorTerrainSettings() {
        this(null, null, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GeneratorTerrainSettings(
            @JsonProperty("fill") @JsonDeserialize(using = GeneratorBlockSelector.class) Supplier<IBlockState> fill,
            @JsonProperty("water") @JsonDeserialize(using = GeneratorBlockSelector.class) Supplier<IBlockState> water,
            @JsonProperty("surface") @JsonDeserialize(using = GeneratorBlockSelector.class) Supplier<IBlockState> surface,
            @JsonProperty("top") @JsonDeserialize(using = GeneratorBlockSelector.class) Supplier<IBlockState> top,
            @JsonProperty("useCwgReplacers") Boolean useCwgReplacers) {
        this.fill = fill != null ? fill : new GeneratorBlockSelector.Single(Blocks.STONE.getDefaultState());
        this.water = water != null ? water : new GeneratorBlockSelector.Single(Blocks.WATER.getDefaultState());
        this.surface = surface != null ? surface : new GeneratorBlockSelector.Single(Blocks.DIRT.getDefaultState());
        this.top = top != null ? top : new GeneratorBlockSelector.Single(Blocks.GRASS.getDefaultState());
        this.useCwgReplacers = fallbackIfNull(useCwgReplacers, true);
    }
}
