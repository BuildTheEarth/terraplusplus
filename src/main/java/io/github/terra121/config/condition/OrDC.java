package io.github.terra121.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.terra121.config.SingleProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
@JsonDeserialize
@Getter(onMethod_ = { @JsonValue })
@SingleProperty
public class OrDC implements DoubleCondition {
    @NonNull
    protected final DoubleCondition[] delegates;

    @Override
    public boolean test(double value) {
        for (DoubleCondition delegate : this.delegates) {
            if (delegate.test(value)) {
                return true;
            }
        }
        return false;
    }
}
