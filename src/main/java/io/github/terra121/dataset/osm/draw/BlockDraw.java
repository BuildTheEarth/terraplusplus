package io.github.terra121.dataset.osm.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.generator.cache.CachedChunkData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;

import java.io.IOException;

import static io.github.terra121.TerraConstants.*;

/**
 * {@link DrawFunction} which sets the surface block to a fixed block state.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(BlockDraw.Parser.class)
@RequiredArgsConstructor
public final class BlockDraw implements DrawFunction {
    @NonNull
    protected final IBlockState state;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfaceBlocks()[x * 16 + z] = this.state;
    }

    static class Parser extends JsonParser<BlockDraw> {
        @Override
        public BlockDraw read(JsonReader in) throws IOException {
            return new BlockDraw(GSON.fromJson(in, IBlockState.class));
        }
    }
}
