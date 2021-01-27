package io.github.terra121.dataset.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.config.scalarparse.d.DoubleScalarParser;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.DoubleTiledDataset;
import io.github.terra121.projection.GeographicProjection;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link DoubleTiledDataset} whose behavior is defined by JSON configuration.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter
public class ConfigurableDoubleTiledDataset extends DoubleTiledDataset {
    protected final String[] urls;
    protected final DoubleScalarParser parse;
    protected final int zoom;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ConfigurableDoubleTiledDataset(
            @JsonProperty(value = "urls", required = true) @NonNull String[] urls,
            @JsonProperty(value = "zoom", required = true) int zoom,
            @JsonProperty(value = "resolution", required = true) int resolution,
            @JsonProperty(value = "blend", required = true) @NonNull BlendMode blend,
            @JsonProperty(value = "parse", required = true) @NonNull DoubleScalarParser parse,
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection) {
        super(projection, positive(resolution, "resolution") << notNegative(zoom, "zoom"), resolution, blend);

        this.urls = urls;
        this.parse = parse;
        this.zoom = zoom;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return this.urls;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        super.addProperties(tileX, tileZ, builder);

        builder.put("zoom", String.valueOf(this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        return this.parse.parse(this.resolution, data);
    }
}
