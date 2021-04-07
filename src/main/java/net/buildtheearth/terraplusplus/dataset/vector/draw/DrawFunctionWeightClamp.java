package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.daporkchop.lib.common.math.PMath;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DrawFunctionWeightClamp implements DrawFunction {
    protected final DrawFunction child;
    protected final int min;
    protected final int max;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DrawFunctionWeightClamp(
            @JsonProperty(value = "child", required = true) @NonNull DrawFunction child,
            @JsonProperty("min") Integer min,
            @JsonProperty("max") Integer max) {
        this.child = child;
        this.min = fallbackIfNull(min, Integer.MIN_VALUE);
        this.max = fallbackIfNull(max, Integer.MAX_VALUE);
    }

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        this.child.drawOnto(data, x, z, PMath.clamp(weight, this.min, this.max));
    }
}
