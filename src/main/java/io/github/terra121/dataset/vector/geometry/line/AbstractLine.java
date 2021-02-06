package io.github.terra121.dataset.vector.geometry.line;

import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.vector.draw.DrawFunction;
import io.github.terra121.dataset.vector.geometry.AbstractVectorGeometry;
import io.github.terra121.dataset.vector.geometry.Segment;
import io.github.terra121.util.bvh.BVH;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractLine extends AbstractVectorGeometry {
    protected final BVH<Segment> segments;

    public AbstractLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines) {
        super(id, layer, draw);

        List<Segment> segments = new ArrayList<>();
        for (LineString line : lines.lines()) { //convert MultiLineString to line segments
            convertToSegments(line, segments);
        }
        this.segments = BVH.of(segments.toArray(new Segment[0]));
    }

    @Override
    public double minX() {
        return this.segments.minX();
    }

    @Override
    public double maxX() {
        return this.segments.maxX();
    }

    @Override
    public double minZ() {
        return this.segments.minZ();
    }

    @Override
    public double maxZ() {
        return this.segments.maxZ();
    }
}
