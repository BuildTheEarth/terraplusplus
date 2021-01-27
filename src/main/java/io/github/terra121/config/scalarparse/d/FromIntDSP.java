package io.github.terra121.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.terra121.config.scalarparse.i.IntScalarParser;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
@JsonDeserialize
@Getter
public class FromIntDSP implements DoubleScalarParser {
    @NonNull
    protected final IntScalarParser delegate;

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
