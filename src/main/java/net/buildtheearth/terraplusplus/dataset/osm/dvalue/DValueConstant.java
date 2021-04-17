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
public final class DValueConstant implements DValue {
    protected final double value;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DValueConstant(
            @JsonProperty(value = "value", required = true) double value) {
        this.value = value;
    }

    @Override
    public double apply(@NonNull Map<String, String> tags) {
        return this.value;
    }
}
