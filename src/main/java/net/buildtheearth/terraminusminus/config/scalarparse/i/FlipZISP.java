package net.buildtheearth.terraminusminus.config.scalarparse.i;

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
public class FlipZISP implements IntScalarParser {
    protected final IntScalarParser delegate;

    @JsonCreator
    public FlipZISP(@JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        for (int z = 0, lim = resolution >> 1; z < lim; z++) {
            for (int x = 0; x < resolution; x++) {
                int a = z * resolution + x;
                int b = (resolution - z - 1) * resolution + x;
                int t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
        return arr;
    }
}
