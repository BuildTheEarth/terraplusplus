package io.github.terra121.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter
public class MultiplyDSP implements DoubleScalarParser {
    protected final DoubleScalarParser delegate;
    protected final double value;

    @JsonCreator
    public MultiplyDSP(
            @JsonProperty(value = "delegate", required = true) @NonNull DoubleScalarParser delegate,
            @JsonProperty(value = "value", required = true) double value) {
        this.delegate = delegate;
        this.value = value;
    }

    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        double[] arr = this.delegate.parse(resolution, buffer);
        double value = this.value;
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            arr[i] *= value;
        }
        return arr;
    }
}
