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
public class AddISP implements IntScalarParser {
    protected final IntScalarParser delegate;
    protected final int value;

    @JsonCreator
    public AddISP(
            @JsonProperty(value = "delegate", required = true) @NonNull IntScalarParser delegate,
            @JsonProperty(value = "value", required = true) int value) {
        this.delegate = delegate;
        this.value = value;
    }

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        int value = this.value;
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            arr[i] += value;
        }
        return arr;
    }
}
