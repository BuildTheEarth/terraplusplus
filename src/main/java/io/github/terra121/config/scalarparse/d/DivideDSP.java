package io.github.terra121.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class DivideDSP extends MultiplyDSP {
    @JsonCreator
    public DivideDSP(
            @JsonProperty(value = "delegate", required = true) @NonNull DoubleScalarParser delegate,
            @JsonProperty(value = "value", required = true) double value) {
        super(delegate, 1.0d / value);
    }
}
