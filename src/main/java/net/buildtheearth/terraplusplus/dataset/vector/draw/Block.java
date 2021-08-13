package net.buildtheearth.terraplusplus.dataset.vector.draw;

import java.io.IOException;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.surface.BlockSurfacePattern;
import net.minecraft.block.state.IBlockState;

/**
 * {@link DrawFunction} which sets the surface block to a fixed block state.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Block.Parser.class)
public final class Block implements DrawFunction {
    @NonNull
    protected final IBlockState state;
    protected transient final BlockSurfacePattern pattern;
    
    public Block(IBlockState state) {
        this.state = state;
        this.pattern = new BlockSurfacePattern(state);
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfacePatterns()[x * 16 + z] = this.pattern;
    }

    static class Parser extends JsonParser<Block> {
        @Override
        public Block read(JsonReader in) throws IOException {
            return new Block(TerraConstants.GSON.fromJson(in, IBlockState.class));
        }
    }
}
