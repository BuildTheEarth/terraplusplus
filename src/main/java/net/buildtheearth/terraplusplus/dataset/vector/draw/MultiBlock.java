package net.buildtheearth.terraplusplus.dataset.vector.draw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.surface.MultiBlockSurfacePattern;
import net.daporkchop.lib.common.util.PValidation;
import net.minecraft.block.state.IBlockState;

/**
 * {@link DrawFunction} which sets the surface to a fixed surface pattern.
 *
 * @author SmylerMC
 */
@JsonAdapter(MultiBlock.Parser.class)
public class MultiBlock implements DrawFunction {
    
    @NonNull
    protected final MultiBlockSurfacePattern pattern;
    
    public MultiBlock(int offset, IBlockState... states) {
        this.pattern = new MultiBlockSurfacePattern(offset, states);
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfacePatterns()[x * 16 + z] = this.pattern;
    }
    
    static class Parser extends JsonParser<MultiBlock> {
        @Override
        public MultiBlock read(JsonReader in) throws IOException {
            in.beginObject();
            int offset = 0;
            List<IBlockState> states = new ArrayList<>();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "offset":
                        offset = in.nextInt();
                        break;
                    case "blocks":
                        in.beginArray();
                        while (in.peek() != JsonToken.END_ARRAY) {
                            states.add(TerraConstants.GSON.fromJson(in, IBlockState.class));
                        }
                        in.endArray();
                        break;
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();
            PValidation.checkState(states.size() > 0, "Illegal block state array: at least one required");
            Collections.reverse(states);
            IBlockState[] blocks = states.toArray(new IBlockState[0]);
            return new MultiBlock(offset, blocks);
        }
    }

}
