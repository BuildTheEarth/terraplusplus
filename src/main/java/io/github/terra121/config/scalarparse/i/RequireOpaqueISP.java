package io.github.terra121.config.scalarparse.i;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
public class RequireOpaqueISP implements IntScalarParser {
    @NonNull
    protected final IntScalarParser delegate;

    @Override
    public int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException {
        int[] arr = this.delegate.parse(resolution, buffer);
        for (int i = 0, len = resolution * resolution; i < len; i++) {
            if ((arr[i] >>> 24) != 0xFF) { //pixel is not fully transparent
                arr[i] = Integer.MIN_VALUE;
            }
        }
        return arr;
    }
}
