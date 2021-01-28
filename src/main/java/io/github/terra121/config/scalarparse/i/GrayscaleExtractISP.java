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
public class GrayscaleExtractISP implements IntScalarParser {
    protected final IntScalarParser delegate;

    @JsonCreator
    public GrayscaleExtractISP(@JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            int val = arr[i];
            arr[i] = (val >>> 24) == 0xFF ? arr[i] & 0x000000FF : Integer.MIN_VALUE;
        }
        return arr;
    }
}
