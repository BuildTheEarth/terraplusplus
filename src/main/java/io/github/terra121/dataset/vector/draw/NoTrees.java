package io.github.terra121.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.osm.JsonParser;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.EarthGeneratorPipelines;
import lombok.NonNull;

import java.io.IOException;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * {@link DrawFunction} which removes trees at a given position.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(NoTrees.Parser.class)
final class NoTrees implements DrawFunction {
    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);
        if (treeCover != null) {
            treeCover[x * 16 + z] = (byte) 0; //set chance to 0
        }
    }

    static class Parser extends JsonParser<NoTrees> {
        @Override
        public NoTrees read(JsonReader in) throws IOException {
            in.beginObject();
            in.endObject();
            return new NoTrees();
        }
    }
}
