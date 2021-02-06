package io.github.terra121.dataset.osm.dvalue;

import com.google.gson.annotations.JsonAdapter;
import lombok.NonNull;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(DValueParser.class)
@FunctionalInterface
public interface DValue {
    double apply(@NonNull Map<String, String> tags);
}
