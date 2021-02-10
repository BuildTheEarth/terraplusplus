package net.buildtheearth.terraplusplus.config.scalarparse.i;

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
public class AndISP implements IntScalarParser {
    protected final IntScalarParser delegate;
    protected final int mask;

    @JsonCreator
    public AndISP(
            @JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate,
            @JsonProperty(value = "mask", required = true) int mask) {
        this.delegate = delegate;
        this.mask = mask;
    }

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        int mask = this.mask;
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            int v = arr[i];
            if (v != Integer.MIN_VALUE) {
                arr[i] = v & mask;
            }
        }
        return arr;
    }
}
