package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.geojson.GeoJSONObject;
import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.Point;
import io.github.terra121.dataset.geojson.geometry.Polygon;
import io.github.terra121.dataset.geojson.object.Feature;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PorkUtil;

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
    public static final OSMBlob EMPTY_BLOB = new OSMBlob(new OSMSegment[0], new OSMPolygon[0], new LineString[0]);

    public static OSMBlob fromGeoJSON(@NonNull GeographicProjection projection, @NonNull GeoJSONObject... objects) {
        List<OSMSegment> segments = new ArrayList<>();
        List<OSMPolygon> polygons = new ArrayList<>();
        List<LineString> waterEdges = new ArrayList<>();

        for (GeoJSONObject object : objects) {
            _fromGeoJSON(projection, segments, polygons, waterEdges, Collections.emptyMap(), object);
        }

        return new OSMBlob(segments.toArray(new OSMSegment[0]), polygons.toArray(new OSMPolygon[0]), waterEdges.toArray(new LineString[0]));
    }

    private static void _fromGeoJSON(GeographicProjection projection, List<OSMSegment> segments, List<OSMPolygon> polygons, List<LineString> waterEdges, Map<String, String> tags, GeoJSONObject object) {
        if (object instanceof Iterable) {
            //recursively process all child elements
            for (GeoJSONObject child : PorkUtil.<Iterable<? extends GeoJSONObject>>uncheckedCast(object)) {
                _fromGeoJSON(projection, segments, polygons, waterEdges, tags, child);
            }
        } else if (object instanceof Feature) {
            //process child using properties from feature
            Feature feature = (Feature) object;
            _fromGeoJSON(projection, segments, polygons, waterEdges, feature.properties() != null ? feature.properties() : tags, feature.geometry());
        } else if (object instanceof LineString) {
            LineString lineString = (LineString) object;
            String natural = tags.get("natural");

            if ("coastline".equals(natural)) {
                waterEdges.add(lineString);
                return;
            }

            SegmentType type = null;
            int lanes = 2;
            int layer = 0;

            String highway = tags.get("highway");
            if (highway != null) {
                if ("yes".equals(tags.get("tunnel"))) { //we don't generate tunnels at all, so don't bother doing anything here
                    return;
                }

                switch (highway) {
                    case "motorway":
                        type = SegmentType.FREEWAY;
                        break;
                    case "trunk":
                        type = SegmentType.LIMITEDACCESS;
                        break;
                    case "motorway_link":
                    case "trunk_link":
                        type = SegmentType.INTERCHANGE;
                        break;
                    case "primary":
                    case "raceway":
                        type = SegmentType.MAIN;
                        break;
                    case "tertiary":
                    case "residential":
                        type = SegmentType.MINOR;
                        break;
                    case "secondary":
                    case "primary_link":
                    case "secondary_link":
                    case "living_street":
                    case "bus_guideway":
                    case "service":
                    case "unclassified":
                        type = SegmentType.SIDE;
                        break;
                    default:
                        type = SegmentType.ROAD;
                }

                String lanesTxt = tags.get("lanes");
                if (lanesTxt != null) {
                    try {
                        lanes = Integer.parseInt(lanesTxt);
                    } catch (NumberFormatException ignored) {
                    }
                }

                String layerTxt = tags.get("layer");
                if (layerTxt != null) {
                    try {
                        layer = Integer.parseInt(layerTxt);
                    } catch (NumberFormatException ignored) {
                    }
                }

                //upgrade road type if many lanes (and the road was important enough to include a lanes tag)
                if (lanes > 2 && type == SegmentType.MINOR) {
                    type = SegmentType.MAIN;
                }
            }

            String building = tags.get("building");
            if (building != null) {
                type = SegmentType.BUILDING;
            }

            String waterway = tags.get("waterway");
            if (waterway != null) {
                switch (waterway) {
                    case "stream":
                        type = SegmentType.STREAM;
                        break;
                    case "river":
                    case "canal":
                        type = SegmentType.RIVER;
                        break;
                }
            }

            if (type == null) { //if we were unable to determine a type, discard this line
                return;
            }

            toSegments(projection, segments, lineString, type, lanes, layer);
        } else if (object instanceof Polygon) {
            Polygon polygon = (Polygon) object;

            if (tags.containsKey("building")) { //this area is a building, and we want buildings to be rendered as an outline rather than a filled polygon
                toSegments(projection, segments, polygon.outerRing(), SegmentType.BUILDING, 1, 0);
                for (LineString innerRing : polygon.innerRings()) {
                    toSegments(projection, segments, innerRing, SegmentType.BUILDING, 1, 0);
                }
                return;
            }

            if (tags.containsKey("water") || "water".equals(tags.get("natural")) || "riverbank".equals(tags.get("waterway"))) {
                toPolygons(projection, polygons, polygon);
            }
        }
    }

    private static void toSegments(GeographicProjection projection, List<OSMSegment> segments, LineString line, SegmentType type, int lanes, int layer) {
        //break line up into projected segments
        double[] lastProj = null;
        for (Point point : line.points()) {
            try {
                double[] proj = projection.fromGeo(point.lon(), point.lat());

                if (lastProj != null) { //register as a road edge
                    segments.add(new OSMSegment(lastProj[0], lastProj[1], proj[0], proj[1], type, lanes, layer));
                }

                lastProj = proj;
            } catch (OutOfProjectionBoundsException e) { //projection is out of bounds
                //set lastProj to null so that it doesn't get attached to a different point further down the line
                lastProj = null;
            }
        }
    }

    private static void toPolygons(GeographicProjection projection, List<OSMPolygon> polygons, Polygon polygon) {
        try {
            double[][][] shapes = new double[1 + polygon.innerRings().length][][];
            int i = 0;
            shapes[i++] = projectPoints(projection, polygon.outerRing().points());
            for (LineString line : polygon.innerRings()) {
                shapes[i++] = projectPoints(projection, line.points());
            }
            polygons.add(new OSMPolygon(shapes));
        } catch (OutOfProjectionBoundsException e) {
            //out of projection bounds, we don't want to output only part of the polygon so we just give up and do nothing
        }
    }

    private static double[][] projectPoints(GeographicProjection projection, Point[] points) throws OutOfProjectionBoundsException {
        double[][] out = new double[points.length][];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            out[i] = projection.fromGeo(point.lon(), point.lat());
        }
        return out;
    }

    public static OSMBlob merge(@NonNull OSMBlob... blobs) {
        return new OSMBlob(
                Arrays.stream(blobs).map(OSMBlob::segments).flatMap(Arrays::stream).toArray(OSMSegment[]::new),
                Arrays.stream(blobs).map(OSMBlob::polygons).flatMap(Arrays::stream).toArray(OSMPolygon[]::new),
                Arrays.stream(blobs).map(OSMBlob::waterEdges).flatMap(Arrays::stream).toArray(LineString[]::new));
    }

    @NonNull
    private final OSMSegment[] segments;
    @NonNull
    private final OSMPolygon[] polygons;
    @NonNull
    private final LineString[] waterEdges;
}
