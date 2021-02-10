package net.buildtheearth.terraplusplus.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A simple closed range between two integers.
 *
 * @author DaPorkchop_
 */
@Getter
@ToString
@EqualsAndHashCode
public class IntRange {
    protected final int min;
    protected final int max;

    @JsonCreator
    public IntRange(int val) {
        this(val, val);
    }

    @JsonCreator
    public IntRange(
            @JsonProperty(value = "min", required = true) int min,
            @JsonProperty(value = "max", required = true) int max) {
        checkArg(min <= max, "min (%d) may not be greater than max (%d)", min, max);
        this.min = min;
        this.max = max;
    }
}
