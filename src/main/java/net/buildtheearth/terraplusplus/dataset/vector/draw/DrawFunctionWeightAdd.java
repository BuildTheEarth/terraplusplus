package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DrawFunctionWeightAdd implements DrawFunction {
    protected final DrawFunction child;
    protected final int value;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DrawFunctionWeightAdd(
            @JsonProperty(value = "child", required = true) @NonNull DrawFunction child,
            @JsonProperty(value = "value", required = true) int value) {
        this.child = child;
        this.value = value;
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        this.child.drawOnto(data, x, z, weight + this.value);
    }
}
