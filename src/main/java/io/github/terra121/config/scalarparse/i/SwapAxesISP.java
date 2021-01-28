package io.github.terra121.config.scalarparse.i;

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
public class SwapAxesISP implements IntScalarParser {
    protected final IntScalarParser delegate;

    @JsonCreator
    public SwapAxesISP(@JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        for (int i = 1; i < resolution; i++) {
            for (int j = 0; j < i; j++) {
                int a = i * resolution + j;
                int b = j * resolution + i;
                int t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
        return arr;
    }
}
