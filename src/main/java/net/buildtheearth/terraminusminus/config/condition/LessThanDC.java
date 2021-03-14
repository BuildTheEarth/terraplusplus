package net.buildtheearth.terraminusminus.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.config.SingleProperty;

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
