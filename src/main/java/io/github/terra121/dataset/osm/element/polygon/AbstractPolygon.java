package io.github.terra121.dataset.osm.element.polygon;

import io.github.terra121.dataset.osm.draw.DrawFunction;
import io.github.terra121.dataset.osm.element.AbstractElement;
import io.github.terra121.dataset.osm.element.Segment;
import io.github.terra121.dataset.osm.geojson.geometry.LineString;
import io.github.terra121.dataset.osm.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.osm.geojson.geometry.Point;
import io.github.terra121.dataset.osm.geojson.geometry.Polygon;
import io.github.terra121.util.interval.IntervalTree;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.terra121.TerraConstants.*;
import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractPolygon extends AbstractElement {
    protected final IntervalTree<Segment> segments;

    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    public AbstractPolygon(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiPolygon polygons) {
        super(id, layer, draw);

        //compute bounds and convert multipolygon to line segments
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        List<Segment> segments = new ArrayList<>();

        for (Polygon polygon : polygons.polygons()) {
            for (Point point : polygon.outerRing().points()) {
                minX = min(minX, point.lon());
                maxX = max(maxX, point.lon());
                minZ = min(minZ, point.lat());
                maxZ = max(maxZ, point.lat());
            }

            checkArg(polygon.outerRing().isLinearRing(), "outer ring must be a closed loop!");
            convertToSegments(polygon.outerRing(), segments);
            for (LineString innerRing : polygon.innerRings()) {
                checkArg(innerRing.isLinearRing(), "inner rings must all be closed loops!");
                convertToSegments(innerRing, segments);
            }
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        this.segments = new IntervalTree<>(segments);
    }

    protected double[] getIntersectionPoints(int pos) {
        int retries = 0;
        double offset = 0.5d;
        do {
            double center = pos + offset;
            List<Segment> segments = this.segments.getAllIntersecting(center);
            if ((segments.size() & 1) == 0) { //if there's an even count, this was successful
                int size = segments.size();
                if (size == 0) {
                    return EMPTY_DOUBLE_ARRAY;
                } else {
                    double[] arr = new double[size];

                    int i = 0;
                    for (Segment s : segments) {
                        arr[i++] = lerp(s.z0(), s.z1(), (s.x0() - center) / (s.x0() - s.x1()));
                    }
                    Arrays.sort(arr);

                    return arr;
                }
            }

            //retry with another random offset
            //this happens because sometimes segments are exactly aligned to the sample grid, in which case everything breaks
            offset = 0.45d + ThreadLocalRandom.current().nextDouble() * 0.1d;
        } while (retries++ < 3);

        //retried multiple times with different offsets and it still failed, abort...
        return EMPTY_DOUBLE_ARRAY;
    }
}
