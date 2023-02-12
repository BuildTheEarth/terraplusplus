package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DrawFunctionConditionalRandom implements DrawFunction {
    protected final DrawFunction child;
    protected final double chance;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DrawFunctionConditionalRandom(
            @JsonProperty(value = "child", required = true) @NonNull DrawFunction child,
            @JsonProperty(value = "chance", required = true) double chance) {
        this.child = child;
        this.chance = chance;
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        if (ThreadLocalRandom.current().nextDouble() <= this.chance) {
            this.child.drawOnto(data, x, z, weight);
        }
    }
}
