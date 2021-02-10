package net.buildtheearth.terraplusplus.util.bvh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Trivial implementation of {@link Bounds2d}.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
@JsonSerialize
@Getter(onMethod_ = { @JsonGetter })
@ToString
@EqualsAndHashCode
class Bounds2dImpl implements Bounds2d {
    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    @JsonCreator
    public Bounds2dImpl(
            @JsonProperty(value = "minX", required = true) double minX,
            @JsonProperty(value = "maxX", required = true) double maxX,
            @JsonProperty(value = "minZ", required = true) double minZ,
            @JsonProperty(value = "maxZ", required = true) double maxZ) {
        checkArg(minX <= maxX, "minX (%s) may not be greater than maxX (%s)!", minX, maxX);
        checkArg(minZ <= maxZ, "minZ (%s) may not be greater than maxZ (%s)!", minZ, maxZ);

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }
}
