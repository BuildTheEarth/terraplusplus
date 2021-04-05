package net.buildtheearth.terraplusplus.util.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.util.stream.IntStream;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class IntSetDeserializeBuilder {
    protected final int[] value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public IntSetDeserializeBuilder(@NonNull int[] arr) {
        this.value = IntStream.of(arr).distinct().sorted().toArray();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public IntSetDeserializeBuilder(@NonNull IntRange range) {
        this.value = IntStream.rangeClosed(range.min(), range.max()).toArray();
    }

    public int[] build() {
        return this.value;
    }
}
