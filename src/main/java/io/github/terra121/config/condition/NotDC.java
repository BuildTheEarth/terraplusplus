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
public class NotDC implements DoubleCondition {
    @NonNull
    protected final DoubleCondition delegate;

    @Override
    public boolean test(double value) {
        return !this.delegate.test(value);
    }
}
