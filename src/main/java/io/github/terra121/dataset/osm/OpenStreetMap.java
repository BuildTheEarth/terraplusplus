package io.github.terra121.dataset.osm;

import com.google.gson.Gson;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.Water;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.projection.GeographicProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenStreetMap extends TiledDataset<OSMRegion> {
    private static final double CHUNK_SIZE = 16;
    public static final double TILE_SIZE = 1 / 60.0;//250*(360.0/40075000.0);

    private final Map<ChunkPos, Set<Segment>> chunks = new LinkedHashMap<>(); //TODO: this leaks memory (it's never drained)
    public final Water water;
    private final List<Segment> allEdges = new ArrayList<>();
    private final Gson gson = new Gson();
    private final boolean doRoad;
    private final boolean doWater;
    private final boolean doBuildings;

    public OpenStreetMap(GeographicProjection proj, boolean doRoad, boolean doWater, boolean doBuildings) {
        super(proj, TILE_SIZE, 1.0d);

        try {
            this.water = new Water(this, 256);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.doRoad = doRoad;
        this.doWater = doWater;
        this.doBuildings = doBuildings;
    }

    @Override
    protected String[] urls() {
        return TerraConfig.data.overpass;
    }

    @Override
    protected OSMRegion decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        OSMRegion region = new OSMRegion(new ChunkPos(tileX, tileZ), this.water);
        this.doGson(new ByteBufInputStream(data), region);

        //TODO: this is ugly and should be implemented better
        {
            double X = tileX * TILE_SIZE;
            double Y = tileZ * TILE_SIZE;

            double[] ll = this.projection.fromGeo(X, Y);
            double[] lr = this.projection.fromGeo(X + TILE_SIZE, Y);
            double[] ur = this.projection.fromGeo(X + TILE_SIZE, Y + TILE_SIZE);
            double[] ul = this.projection.fromGeo(X, Y + TILE_SIZE);

            //estimate bounds of region in terms of chunks
            int lowX = (int) Math.floor(Math.min(Math.min(ll[0], ul[0]), Math.min(lr[0], ur[0])) / CHUNK_SIZE);
            int highX = (int) Math.ceil(Math.max(Math.max(ll[0], ul[0]), Math.max(lr[0], ur[0])) / CHUNK_SIZE);
            int lowZ = (int) Math.floor(Math.min(Math.min(ll[1], ul[1]), Math.min(lr[1], ur[1])) / CHUNK_SIZE);
            int highZ = (int) Math.ceil(Math.max(Math.max(ll[1], ul[1]), Math.max(lr[1], ur[1])) / CHUNK_SIZE);

            for (Segment e : this.allEdges) {
                this.relevantChunks(lowX, lowZ, highX, highZ, e);
            }
            this.allEdges.clear();
        }

        return region;
    }

    public ChunkPos getRegion(double lon, double lat) {
        return new ChunkPos((int) Math.floor(lon / TILE_SIZE), (int) Math.floor(lat / TILE_SIZE));
    }

    public Set<Segment> chunkStructures(int x, int z) {
        ChunkPos coord = new ChunkPos(x, z);

        if (this.regionCache(this.projection.toGeo(x * CHUNK_SIZE, z * CHUNK_SIZE)) == null) {
            return null;
        }

        if (this.regionCache(this.projection.toGeo((x + 1) * CHUNK_SIZE, z * CHUNK_SIZE)) == null) {
            return null;
        }

        if (this.regionCache(this.projection.toGeo((x + 1) * CHUNK_SIZE, (z + 1) * CHUNK_SIZE)) == null) {
            return null;
        }

        if (this.regionCache(this.projection.toGeo(x * CHUNK_SIZE, (z + 1) * CHUNK_SIZE)) == null) {
            return null;
        }

        return this.chunks.get(coord);
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

        Map<Long, Element> allWays = new HashMap<>();
        Set<Element> unusedWays = new HashSet<>();
        Set<Long> ground = new HashSet<>();

        for (Element elem : data.elements) {
            Attributes attributes = Attributes.NONE;
            if (elem.type == EType.way) {
                allWays.put(elem.id, elem);

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
                    this.waterway(elem, -1, region, null);
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
                        for (Member member : elem.members) {
                            if (member.type == EType.way) {
                                Element way = allWays.get(member.ref);
                                if (way != null) {
                                    this.waterway(way, elem.id + 3600000000L, region, null);
                                    unusedWays.remove(way);
                                }
                            }
                        }
                        continue;
                    }
                }
                if (this.doBuildings && elem.tags.get("building") != null) {
                    for (Member member : elem.members) {
                        if (member.type == EType.way) {
                            Element way = allWays.get(member.ref);
                            if (way != null) {
                                this.addWay(way, SegmentType.BUILDING, (byte) 1, region, Attributes.NONE, (byte) 0);
                                unusedWays.remove(way);
                            }
                        }
                    }
                }

            } else if (elem.type == EType.area) {
                ground.add(elem.id);
            }
        }

        if (this.doWater) {
            for (Element way : unusedWays) {
                if (way.tags != null) {
                    String naturalv = way.tags.get("natural");
                    String waterv = way.tags.get("water");
                    String wway = way.tags.get("waterway");

                    if (waterv != null || "water".equals(naturalv) || "riverbank".equals(wway)) {
                        this.waterway(way, way.id + 2400000000L, region, null);
                    }
                }
            }

            if (this.water.grounding.state(region.coord.x, region.coord.z) == 0) {
                ground.add(-1L);
            }

            region.renderWater(ground);
        }
    }

    void addWay(Element elem, SegmentType type, byte lanes, OSMRegion region, Attributes attributes, byte layer) {
        double[] lastProj = null;
        if (elem.geometry != null) {
            for (Geometry geom : elem.geometry) {
                if (geom == null) {
                    lastProj = null;
                } else {
                    double[] proj = this.projection.fromGeo(geom.lon, geom.lat);

                    if (lastProj != null) { //register as a road edge
                        this.allEdges.add(new Segment(lastProj[0], lastProj[1], proj[0], proj[1], type, lanes, region, attributes, layer));
                    }

                    lastProj = proj;
                }
            }
        }
    }

    Geometry waterway(Element way, long id, OSMRegion region, Geometry last) {
        if (way.geometry != null) {
            for (Geometry geom : way.geometry) {
                if (geom != null && last != null) {
                    region.addWaterEdge(last.lon, last.lat, geom.lon, geom.lat, id);
                }
                last = geom;
            }
        }

        return last;
    }

    private void relevantChunks(int lowX, int lowZ, int highX, int highZ, Segment edge) {
        ChunkPos start = new ChunkPos((int) Math.floor(edge.lon0 / CHUNK_SIZE), (int) Math.floor(edge.lat0 / CHUNK_SIZE));
        ChunkPos end = new ChunkPos((int) Math.floor(edge.lon1 / CHUNK_SIZE), (int) Math.floor(edge.lat1 / CHUNK_SIZE));

        double startx = edge.lon0;
        double endx = edge.lon1;

        if (startx > endx) {
            ChunkPos tmp = start;
            start = end;
            end = tmp;
            startx = endx;
            endx = edge.lon0;
        }

        highX = Math.min(highX, end.x + 1);
        for (int x = Math.max(lowX, start.x); x < highX; x++) {
            double X = x * CHUNK_SIZE;
            int from = (int) Math.floor((edge.slope * Math.max(X, startx) + edge.offset) / CHUNK_SIZE);
            int to = (int) Math.floor((edge.slope * Math.min(X + CHUNK_SIZE, endx) + edge.offset) / CHUNK_SIZE);

            if (from > to) {
                int tmp = from;
                from = to;
                to = tmp;
            }

            for (int z = Math.max(from, lowZ); z <= to && z < highZ; z++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        this.assoiateWithChunk(new ChunkPos(x + dx, z + dz), edge);
                    }
                }
                //this.assoiateWithChunk(new ChunkPos(x, z), edge);
            }
        }
    }

    private void assoiateWithChunk(ChunkPos c, Segment edge) {
        Set<Segment> list = this.chunks.get(c);
        if (list == null) {
            list = new HashSet<>();
            this.chunks.put(c, list);
        }
        list.add(edge);
    }

    public enum Attributes {
        ISBRIDGE, ISTUNNEL, NONE
    }

    public enum EType {
        invalid, node, way, relation, area
    }

    public static class Member {
        EType type;
        long ref;
        String role;
    }

    public static class Geometry {
        double lat;
        double lon;
    }

    public static class Element {
        EType type;
        long id;
        Map<String, String> tags;
        long[] nodes;
        Member[] members;
        Geometry[] geometry;
    }

    public static class Data {
        float version;
        String generator;
        Map<String, String> osm3s;
        List<Element> elements;
    }
}