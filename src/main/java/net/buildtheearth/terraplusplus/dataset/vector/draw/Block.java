package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.minecraft.block.state.IBlockState;

import java.io.IOException;

/**
 * {@link DrawFunction} which sets the surface block to a fixed block state.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Block.Parser.class)
@RequiredArgsConstructor
public final class Block implements DrawFunction {
    @NonNull
    protected final IBlockState state;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfaceBlocks()[x * 16 + z] = this.state;
    }

    static class Parser extends JsonParser<Block> {
        @Override
        public Block read(JsonReader in) throws IOException {
            return new Block(TerraConstants.GSON.fromJson(in, IBlockState.class));
        }
    }
}
