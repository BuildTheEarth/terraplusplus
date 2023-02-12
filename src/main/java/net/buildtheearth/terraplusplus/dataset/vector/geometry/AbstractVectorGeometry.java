package net.buildtheearth.terraplusplus.dataset.vector.geometry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.util.jackson.IntRange;

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
    protected final IntRange levels;

    protected boolean containsZoom(int zoom) {
        return this.levels == null || this.levels.contains(zoom);
    }
}
