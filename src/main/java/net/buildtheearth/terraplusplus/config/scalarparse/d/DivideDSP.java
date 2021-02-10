package net.buildtheearth.terraplusplus.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class DivideDSP implements DoubleScalarParser {
    protected final DoubleScalarParser delegate;
    protected final double value;

    @Getter(AccessLevel.NONE)
    protected final double factor;

    @JsonCreator
    public DivideDSP(
            @JsonProperty(value = "delegate", required = true) @NonNull DoubleScalarParser delegate,
            @JsonProperty(value = "value", required = true) double value) {
        this.delegate = delegate;
        this.value = value;
        this.factor = 1.0d / value;
    }

    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        double[] arr = this.delegate.parse(resolution, buffer);
        double factor = this.factor;
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            arr[i] *= factor;
        }
        return arr;
    }
}
