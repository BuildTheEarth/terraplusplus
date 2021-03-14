package net.buildtheearth.terraminusminus.config.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class NotDC implements DoubleCondition {
    protected final DoubleCondition delegate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NotDC(@JsonProperty(value = "delegate", required = true) @NonNull DoubleCondition delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean test(double value) {
        return !this.delegate.test(value);
    }
}
