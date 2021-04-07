package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.minecraft.block.state.IBlockState;

/**
 * {@link DrawFunction} which sets the surface block to a fixed block state.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DrawFunctionBlock implements DrawFunction {
    protected final IBlockState state;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DrawFunctionBlock(
            @JsonProperty(value = "state", required = true) @NonNull IBlockState state) {
        this.state = state;
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfaceBlocks()[x * 16 + z] = this.state;
    }
}
