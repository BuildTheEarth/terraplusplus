package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Polygon;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class PolygonMapperConvertToLines implements PolygonMapper {
    protected final LineMapper next;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PolygonMapperConvertToLines(
            @JsonProperty(value = "next", required = true) @NonNull LineMapper next) {
        this.next = next;
    }

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiPolygon projectedGeometry) {
        //convert multipolygon to multilinestring
        List<LineString> lines = new ArrayList<>();
        for (Polygon polygon : projectedGeometry.polygons()) {
            lines.add(polygon.outerRing());
            lines.addAll(Arrays.asList(polygon.innerRings()));
        }

        return this.next.apply(id, tags, originalGeometry, new MultiLineString(lines.toArray(new LineString[0])));
    }
}
