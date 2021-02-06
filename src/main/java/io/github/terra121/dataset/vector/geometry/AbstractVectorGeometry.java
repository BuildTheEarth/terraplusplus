package io.github.terra121.dataset.vector.geometry;

import io.github.terra121.dataset.vector.geojson.geometry.LineString;
import io.github.terra121.dataset.vector.geojson.geometry.Point;
import io.github.terra121.dataset.vector.draw.DrawFunction;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractVectorGeometry implements VectorGeometry {
    protected static void convertToSegments(@NonNull LineString line, @NonNull List<Segment> segments) {
        Point[] points = line.points();
        Point prev = points[0];
        for (int i = 1; i < points.length; i++) {
            Point next = points[i];
            segments.add(new Segment(prev.lon(), prev.lat(), next.lon(), next.lat()));
            prev = next;
        }
    }

    @NonNull
    protected final String id;
    protected final double layer;
    @NonNull
    protected final DrawFunction draw;
}
