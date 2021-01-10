package io.github.terra121.dataset.osm.config;

import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.LineString;

/**
 * @author DaPorkchop_
 */
public abstract class OSMEmitter<G extends Geometry> {
    public static final class NarrowLine extends OSMEmitter<LineString> {
    }

    public static final class WideLine extends OSMEmitter<LineString> {
    }
}
