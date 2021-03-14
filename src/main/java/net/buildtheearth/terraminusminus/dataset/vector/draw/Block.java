package net.buildtheearth.terraminusminus.dataset.vector.draw;

import java.io.IOException;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.osm.JsonParser;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.state.IBlockState;

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
