package net.buildtheearth.terraminusminus.dataset.vector.geometry;

import net.buildtheearth.terraminusminus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraminusminus.dataset.vector.draw.DrawFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
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
