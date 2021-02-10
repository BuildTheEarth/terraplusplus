package net.buildtheearth.terraplusplus.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.config.SingleProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
@JsonDeserialize
@Getter(onMethod_ = { @JsonValue })
@SingleProperty
public class LessThanDC implements DoubleCondition {
    protected final double value;

    @Override
    public boolean test(double value) {
        return value < this.value;
    }
}
