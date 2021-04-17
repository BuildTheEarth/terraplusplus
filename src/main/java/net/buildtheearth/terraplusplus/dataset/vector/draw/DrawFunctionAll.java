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
public final class DrawFunctionAll implements DrawFunction {
    protected final DrawFunction[] children;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DrawFunctionAll(
            @JsonProperty(value = "children", required = true) @NonNull DrawFunction[] children) {
        this.children = children;
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        for (DrawFunction delegate : this.children) {
            delegate.drawOnto(data, x, z, weight);
        }
    }
}
