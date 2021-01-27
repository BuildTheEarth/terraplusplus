package io.github.terra121.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator })
@JsonDeserialize
@Getter
public class AndDC implements DoubleCondition {
    @NonNull
    protected final DoubleCondition[] delegates;

    @Override
    public boolean test(double value) {
        for (DoubleCondition delegate : this.delegates) {
            if (!delegate.test(value)) {
                return false;
            }
        }
        return true;
    }
}
