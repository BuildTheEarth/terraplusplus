package io.github.terra121.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.terra121.config.scalarparse.i.IntScalarParser;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class FromIntDSP implements DoubleScalarParser {
    protected final IntScalarParser delegate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FromIntDSP(@JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] src = this.delegate.parse(resolution, buffer);
        int len = resolution * resolution;
        double[] dst = new double[len];
        for (int i = 0; i < len; i++) {
            dst[i] = src[i] != Integer.MIN_VALUE ? src[i] : Double.NaN;
        }
        return dst;
    }
}
