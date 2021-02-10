package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import lombok.NonNull;

import java.io.IOException;

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
