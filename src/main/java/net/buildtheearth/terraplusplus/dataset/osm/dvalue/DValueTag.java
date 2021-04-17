package net.buildtheearth.terraplusplus.dataset.osm.dvalue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

/**
 * Returns a single, constant value.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DValueTag implements DValue {
    protected final String key;
    protected final double fallback;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DValueTag(
            @JsonProperty(value = "key", required = true) @NonNull String key,
            @JsonProperty(value = "fallback", required = true) double fallback) {
        this.key = key.intern();
        this.fallback = fallback;
    }

    @Override
    public double apply(@NonNull Map<String, String> tags) {
        String value = tags.get(this.key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return this.fallback;
    }
}
