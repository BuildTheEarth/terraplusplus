package io.github.terra121.dataset.osm.element.line;

import io.github.terra121.dataset.osm.geojson.geometry.LineString;
import io.github.terra121.dataset.osm.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.draw.DrawFunction;
import io.github.terra121.dataset.osm.element.AbstractElement;
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
public abstract class AbstractLine extends AbstractElement {
    protected final BVH<Segment> segments;

    public AbstractLine(@NonNull String id, double layer, @NonNull DrawFunction draw, @NonNull MultiLineString lines) {
        super(id, layer, draw);

        List<Segment> segments = new ArrayList<>();
        for (LineString line : lines.lines()) { //convert MultiLineString to line segments
            convertToSegments(line, segments);
        }
        this.segments = new BVH<>(segments);
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
