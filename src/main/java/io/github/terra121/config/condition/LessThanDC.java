package io.github.terra121.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator })
@JsonDeserialize
@Getter
public class LessThanDC implements DoubleCondition {
    protected final double value;

    @Override
    public boolean test(double value) {
        return value < this.value;
    }
}
