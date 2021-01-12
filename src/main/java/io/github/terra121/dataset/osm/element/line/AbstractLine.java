package io.github.terra121.dataset.osm.element.line;

import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.geojson.geometry.Point;
import io.github.terra121.dataset.osm.draw.DrawFunction;
import io.github.terra121.dataset.osm.element.Element;
import io.github.terra121.dataset.osm.element.Segment;
import io.github.terra121.util.bvh.BVH;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractLine implements Element {
    protected final BVH<Segment> segments;
    protected final DrawFunction draw;
    protected final double layer;

    public AbstractLine(@NonNull MultiLineString lines, @NonNull DrawFunction draw, double layer) {
        this.draw = draw;
        this.layer = layer;

        List<Segment> segments = new ArrayList<>();
        for (LineString line : lines.lines()) { //convert MultiLineString to line segments
            Point[] points = line.points();
            Point prev = points[0];
            for (int i = 1; i < points.length; i++) {
                Point next = points[i];
                segments.add(new Segment(prev.lon(), prev.lat(), next.lon(), next.lat()));
                prev = next;
            }
        }
        this.segments = new BVH<>(segments);
    }
}
