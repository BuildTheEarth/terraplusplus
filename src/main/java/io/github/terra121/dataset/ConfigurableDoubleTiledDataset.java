package io.github.terra121.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.terra121.config.scalarparse.d.DoubleScalarParser;
import io.github.terra121.projection.GeographicProjection;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;

/**
 * Implementation of {@link DoubleTiledDataset} whose behavior is defined by JSON configuration.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
@JsonSerialize
@Getter(onMethod_ = { @JsonGetter })
public class ConfigurableDoubleTiledDataset extends DoubleTiledDataset {
    protected final String[] urls;
    protected final DoubleScalarParser parse;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ConfigurableDoubleTiledDataset(
            @JsonProperty(value = "urls", required = true) @NonNull String[] urls,
            @JsonProperty(value = "resolution", required = true) int resolution,
            @JsonProperty(value = "blend", required = true) @NonNull BlendMode blend,
            @JsonProperty(value = "parse", required = true) @NonNull DoubleScalarParser parse,
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection) {
        super(projection, resolution, blend);

        this.urls = urls;
        this.parse = parse;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return this.urls;
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        return this.parse.parse(this.resolution, data);
    }

    @Override
    @JsonGetter
    public int resolution() {
        return super.resolution();
    }

    @Override
    @JsonGetter
    public BlendMode blend() {
        return super.blend();
    }

    @Override
    @JsonGetter
    public GeographicProjection projection() {
        return super.projection();
    }
}
