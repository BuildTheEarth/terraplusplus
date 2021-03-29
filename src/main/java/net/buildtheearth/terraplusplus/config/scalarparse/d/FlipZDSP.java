package net.buildtheearth.terraplusplus.config.scalarparse.d;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
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
@Getter(onMethod_ = { @JsonGetter })
public class FlipZDSP implements DoubleScalarParser {
    protected final DoubleScalarParser delegate;

    @JsonCreator
    public FlipZDSP(@JsonProperty(value = "delegate", required = true) @NonNull DoubleScalarParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        double[] arr = this.delegate.parse(resolution, buffer);
        for (int z = 0, lim = resolution >> 1; z < lim; z++) {
            for (int x = 0; x < resolution; x++) {
                int a = z * resolution + x;
                int b = (resolution - z - 1) * resolution + x;
                double t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
        return arr;
    }
}
