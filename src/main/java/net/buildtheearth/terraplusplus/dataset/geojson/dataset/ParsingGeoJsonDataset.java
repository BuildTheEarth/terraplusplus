package net.buildtheearth.terraplusplus.dataset.geojson.dataset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.KeyedHttpDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.daporkchop.lib.common.function.io.IOFunction;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
public class ParsingGeoJsonDataset extends KeyedHttpDataset<GeoJsonObject[]> {
    public ParsingGeoJsonDataset(@NonNull String[] urls) {
        super(urls);
    }

    @Override
    protected GeoJsonObject[] decode(@NonNull String path, @NonNull ByteBuf data) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteBufInputStream(data)))) { //parse each line as a GeoJSON object
            return reader.lines().map((IOFunction<String, GeoJsonObject>) s -> JSON_MAPPER.readValue(s, GeoJsonObject.class)).toArray(GeoJsonObject[]::new);
        }
    }
}
