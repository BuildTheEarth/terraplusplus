package io.github.terra121.dataset.geojson;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class GeoJSON {
    public static void main(String... args) throws IOException {
        String[] jsons = {
                "{\"type\":\"Point\",\"coordinates\":[7.6216509,47.153135]}",
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[7.6216509,47.153135]},\"properties\":{\"natural\":\"natural\",\"source\":\"source\"}}"
        };

        Gson gson = new Gson();
        for (String json : jsons) {
            System.out.println(gson.fromJson(json, GeoJSONObject.class));
        }
    }
}
