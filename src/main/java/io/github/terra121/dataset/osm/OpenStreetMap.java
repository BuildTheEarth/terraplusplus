package io.github.terra121.dataset.osm;

import com.google.gson.Gson;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.osm.poly.Polygon;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

public class OpenStreetMap extends TiledDataset<OSMRegion> {
    //protected static final String QUERY_SUFFIX = "?data=[out:json];way(${lat.min},${lon.min},${lat.max},${lon.max});out%20geom(${lat.min},${lon.min},${lat.max},${lon.max})%20tags%20qt;(._<;);out%20body%20qt;is_in(${lat.min},${lon.min});area._[~\"natural|waterway\"~\"water|riverbank\"];out%20ids;";
    protected static final String QUERY_SUFFIX = "?data=[out:json];way(${lat.min},${lon.min},${lat.max},${lon.max});out%20geom%20tags%20qt;(._<;);out%20body%20qt;is_in(${lat.min},${lon.min});area._[~\"natural|waterway\"~\"water|riverbank\"];out%20ids;";

    public static final double TILE_SIZE = 1 / 60.0;//250*(360.0/40075000.0);

    protected final GeographicProjection earthProjection;

    private final Ref<List<Segment>> allSegments = ThreadRef.soft(ArrayList::new);
    private final Ref<List<Polygon>> allPolygons = ThreadRef.soft(ArrayList::new);
    private final Gson gson = new Gson();
    private final boolean doRoad;
    private final boolean doWater;
    private final boolean doBuildings;

    public OpenStreetMap(@NonNull GeographicProjection earthProjection, boolean doRoad, boolean doWater, boolean doBuildings) {
        super(new EquirectangularProjection(), TILE_SIZE);

        this.earthProjection = earthProjection;

        this.doRoad = doRoad;
        this.doWater = doWater;
        this.doBuildings = doBuildings;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return Arrays.stream(TerraConfig.data.overpass)
                .map(s -> s + QUERY_SUFFIX)
                .toArray(String[]::new);
    }

    @Override
    protected OSMRegion decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        List<Segment> allSegments = this.allSegments.get();
        List<Polygon> allPolygons = this.allPolygons.get(); //store here to prevent them from being GC'd
        try {
            OSMRegion region = new OSMRegion(new ChunkPos(tileX, tileZ));
            this.doGson(new ByteBufInputStream(data), region);

            region.segments = new BVH<>(allSegments);
            region.polygons = new BVH<>(allPolygons);

            return region;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        } finally {
            allSegments.clear();
            allPolygons.clear();
        }
    }

    public ChunkPos getRegion(double lon, double lat) {
        return new ChunkPos(floorI(lon / TILE_SIZE), floorI(lat / TILE_SIZE));
    }

    public CompletableFuture<OSMRegion[]> getRegionsAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        Bounds2d localBounds = bounds.fromGeo(this.projection).axisAlign();
        CompletableFuture<OSMRegion>[] futures = uncheckedCast(Arrays.stream(localBounds.toTiles(TILE_SIZE))
                .map(this::getTileAsync)
                .toArray(CompletableFuture[]::new));

        return CompletableFuture.allOf(futures)
                .thenApplyAsync(unused -> Arrays.stream(futures).map(CompletableFuture::join).toArray(OSMRegion[]::new));
    }

    public OSMRegion regionCache(double[] corner) {
        //bound check
        if (!(corner[0] >= -180 && corner[0] <= 180 && corner[1] >= -80 && corner[1] <= 80)) {
            return null;
        }

        ChunkPos coord = this.getRegion(corner[0], corner[1]);
        return this.getTile(coord.x, coord.z);
    }

    private void doGson(InputStream is, OSMRegion region) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8);

        Data data = this.gson.fromJson(writer.toString(), Data.class);

        Long2ObjectMap<Element> idToElement = new Long2ObjectOpenHashMap<>();
        Set<Element> unusedWays = new HashSet<>();

        //pass 1: build id -> element map
        for (Element elem : data.elements) {
            checkState(idToElement.put(elem.id, elem) == null, elem.id);
        }

        //pass 2: assemble multipolygons
        for (Element elem : data.elements) {
            if (elem.type != EType.relation || elem.members == null || elem.tags == null || !"multipolygon".equals(elem.tags.get("type"))) {
                continue;
            }

            List<Geometry> out = new ArrayList<>();
            Map<Geometry, List<Geometry>> points = new HashMap<>();
            for (Member member : elem.members) {
                if (member.type != EType.way) {
                    continue;
                }
                Element way = idToElement.get(member.ref);
                if (way == null || way.geometry == null) {
                    continue;
                }

                try {
                    Geometry head = way.geometry[0];
                    Geometry tail = way.geometry[way.geometry.length - 1];

                    if (Objects.equals(head, tail)) { //way is a closed loop
                        out.addAll(Arrays.asList(way.geometry));
                        continue;
                    }

                    Geometry matchingPoint;
                    List<Geometry> listOut;
                    if ((listOut = points.get(matchingPoint = head)) != null) {
                        listOut.addAll(0, Arrays.asList(Arrays.copyOfRange(way.geometry, 0, way.geometry.length - 1))); //append to front
                    } else if ((listOut = points.get(matchingPoint = tail)) != null) {
                        listOut.addAll(Arrays.asList(Arrays.copyOfRange(way.geometry, 1, way.geometry.length))); //append to tail
                    }

                    if (listOut != null) {
                        points.remove(matchingPoint);
                        if (Objects.equals(listOut.get(0), listOut.get(listOut.size() - 1))) { //closed loop
                            out.addAll(listOut);
                            points.remove(listOut.get(0), listOut);
                            points.remove(listOut.get(listOut.size() - 1), listOut);
                            continue;
                        }
                    } else {
                        listOut = new ArrayList<>(Arrays.asList(way.geometry));
                    }

                    points.put(listOut.get(0), listOut);
                    points.put(listOut.get(listOut.size() - 1), listOut);
                } finally {
                    way.geometry = null;
                }
            }

            while (!points.isEmpty()) { //hacky solution: fix all points
                List<Geometry> list = points.values().iterator().next();
                points.values().removeIf(v -> v == list);
                out.addAll(list);
                out.add(list.get(0));
            }

            elem.geometry = out.toArray(new Geometry[0]);
        }

        for (Element elem : data.elements) {
            Attributes attributes = Attributes.NONE;
            if (elem.type == EType.way) {
                if (elem.tags == null) {
                    unusedWays.add(elem);
                    continue;
                }

                String naturalv = null;
                String highway = null;
                String waterway = null;
                String building = null;
                String istunnel = null;
                String isbridge = null;

                if (this.doWater) {
                    naturalv = elem.tags.get("natural");
                    waterway = elem.tags.get("waterway");
                }

                if (this.doRoad) {
                    highway = elem.tags.get("highway");
                    istunnel = elem.tags.get("tunnel");
                    // to be implemented
                    isbridge = elem.tags.get("bridge");
                }

                if (this.doBuildings) {
                    building = elem.tags.get("building");
                }

                if ("coastline".equals(naturalv)) {
                    this.waterway(elem);
                } else if (highway != null || building != null || ("river".equals(waterway) || "canal".equals(waterway) || "stream".equals(waterway))) { //TODO: fewer equals
                    SegmentType type = SegmentType.ROAD;

                    if (waterway != null) {
                        type = SegmentType.STREAM;
                        if ("river".equals(waterway) || "canal".equals(waterway)) {
                            type = SegmentType.RIVER;
                        }
                    }

                    if (building != null) {
                        type = SegmentType.BUILDING;
                    }

                    if ("yes".equals(istunnel)) {
                        attributes = Attributes.ISTUNNEL;
                        continue; //tunnels are never generated, don't bother adding them
                    } else if ("yes".equals(isbridge)) {
                        attributes = Attributes.ISBRIDGE;
                    }

                    if (highway != null) {
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
                        }
                    }

                    //get lane number (default is 2)
                    String slanes = elem.tags.get("lanes");
                    String slayer = elem.tags.get("layer");
                    byte lanes = 2;
                    byte layer = 0;

                    if (slayer != null) {
                        try {
                            layer = Byte.parseByte(slayer);
                        } catch (NumberFormatException e) {
                        }
                    }

                    if (slanes != null) {
                        try {
                            lanes = Byte.parseByte(slanes);
                        } catch (NumberFormatException e) {
                        }
                    }

                    //prevent super high # of lanes to prevent ridiculous results (prly a mistake if its this high anyways)
                    if (lanes > 8) {
                        lanes = 8;
                    }

                    // an interchange that doesn't have any lane tag should be defaulted to 2 lanes
                    if (lanes < 2 && type == SegmentType.INTERCHANGE) {
                        lanes = 2;
                    }

                    // upgrade road type if many lanes (and the road was important enough to include a lanes tag)
                    if (lanes > 2 && type == SegmentType.MINOR) {
                        type = SegmentType.MAIN;
                    }

                    this.addWay(elem, type, lanes, region, attributes, layer);
                } else {
                    unusedWays.add(elem);
                }
            } else if (elem.type == EType.relation && elem.members != null && elem.tags != null) {
                if (this.doWater) {
                    String naturalv = elem.tags.get("natural");
                    String waterv = elem.tags.get("water");
                    String wway = elem.tags.get("waterway");

                    if (waterv != null || "water".equals(naturalv) || "riverbank".equals(wway)) {
                        if (elem.geometry != null) {
                            this.waterway(elem);
                        } else {
                            for (Member member : elem.members) {
                                if (member.type == EType.way) {
                                    Element way = idToElement.get(member.ref);
                                    if (way != null) {
                                        this.waterway(way);
                                        unusedWays.remove(way);
                                    }
                                }
                            }
                        }
                        continue;
                    }
                }
                if (this.doBuildings && elem.tags.get("building") != null) {
                    for (Member member : elem.members) {
                        if (member.type == EType.way) {
                            Element way = idToElement.get(member.ref);
                            if (way != null) {
                                this.addWay(way, SegmentType.BUILDING, (byte) 1, region, Attributes.NONE, (byte) 0);
                                unusedWays.remove(way);
                            }
                        }
                    }
                }
            }
        }

        if (this.doWater) {
            for (Element way : unusedWays) {
                if (way.tags != null) {
                    String naturalv = way.tags.get("natural");
                    String waterv = way.tags.get("water");
                    String wway = way.tags.get("waterway");

                    if (waterv != null || "water".equals(naturalv) || "riverbank".equals(wway)) {
                        this.waterway(way);
                    }
                }
            }
        }
    }

    void addWay(Element elem, SegmentType type, byte lanes, OSMRegion region, Attributes attributes, byte layer) {
        double[] lastProj = null;
        if (elem.geometry != null) {
            for (Geometry geom : elem.geometry) {
                if (geom == null) {
                    lastProj = null;
                } else {
                    try {
                        double[] proj = this.earthProjection.fromGeo(geom.lon, geom.lat);

                        if (lastProj != null) { //register as a road edge
                            this.allSegments.get().add(new Segment(lastProj[0], lastProj[1], proj[0], proj[1], type, lanes, region, attributes, layer));
                        }

                        lastProj = proj;
                    } catch (OutOfProjectionBoundsException e) { //projection is out of bounds, make this point unusable
                        lastProj = null;
                    }
                }
            }
        }
    }

    Geometry waterway(Element way) {
        Geometry last = null;
        if (way.geometry != null) {
            double[][][] poly = this.buildPolygon(way.geometry);
            if (poly != null) {
                this.allPolygons.get().add(new Polygon(poly));
            }
        }
        return last;
    }

    protected double[][][] buildPolygon(@NonNull Geometry[] in) {
        List<double[][]> shapes = new ArrayList<>();
        List<double[]> points = new ArrayList<>();
        Set<Geometry> usedPoints = new HashSet<>();

        for (Geometry geometry : in) {
            if (usedPoints.add(geometry)) {
                try {
                    points.add(this.earthProjection.fromGeo(geometry.lon, geometry.lat));
                } catch (OutOfProjectionBoundsException e) { //skip point
                    throw new RuntimeException(geometry.toString(), e);
                }
            } else {
                shapes.add(points.toArray(new double[0][]));
                points.clear();
                usedPoints.clear();
            }
        }

        return shapes.isEmpty() ? null : shapes.toArray(new double[0][][]);
    }

    public enum Attributes {
        ISBRIDGE, ISTUNNEL, NONE
    }

    public enum EType {
        invalid, node, way, relation, area
    }

    @ToString
    public static class Member {
        EType type;
        long ref;
        String role;
    }

    @ToString
    @EqualsAndHashCode
    public static class Geometry {
        double lat;
        double lon;
    }

    @ToString
    public static class Element {
        EType type;
        long id;
        Map<String, String> tags;
        long[] nodes;
        Member[] members;
        @ToString.Exclude
        Geometry[] geometry;
    }

    public static class Data {
        float version;
        String generator;
        Map<String, String> osm3s;
        List<Element> elements;
    }
}