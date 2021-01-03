package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.geojson.GeoJSONObject;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An unstructured collection of parsed OpenStreetMap data.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public final class OSMBlob {
    public static OSMBlob fromGeoJSON(@NonNull GeoJSONObject... objects) {
        List<OSMSegment> segments = new ArrayList<>();
        List<OSMPolygon> polygons = new ArrayList<>();

        for (GeoJSONObject object : objects) {
            _fromGeoJSON(segments, polygons, Collections.emptyMap(), object);
        }

        return new OSMBlob(segments.toArray(new OSMSegment[0]), polygons.toArray(new OSMPolygon[0]));
    }

    private static void _fromGeoJSON(List<OSMSegment> segments, List<OSMPolygon> polygons, Map<String, String> tags, GeoJSONObject object) {
    }

    public static OSMBlob merge(@NonNull OSMBlob... blobs) {
        return new OSMBlob(
                Arrays.stream(blobs).map(OSMBlob::segments).flatMap(Arrays::stream).toArray(OSMSegment[]::new),
                Arrays.stream(blobs).map(OSMBlob::polygons).flatMap(Arrays::stream).toArray(OSMPolygon[]::new));
    }

    @NonNull
    private final OSMSegment[] segments;
    @NonNull
    private final OSMPolygon[] polygons;
}
