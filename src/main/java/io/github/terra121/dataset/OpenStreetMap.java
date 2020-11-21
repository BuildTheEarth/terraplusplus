package io.github.terra121.dataset;

import com.google.gson.Gson;
import io.github.terra121.TerraConfig;
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

public class OpenStreetMap extends TiledDataset<Region> {
    private static final double CHUNK_SIZE = 16;
    public static final double TILE_SIZE = 1 / 60.0;//250*(360.0/40075000.0);
    private static final double NOTHING = 0.01;

    private final Map<ChunkPos, Set<Edge>> chunks = new LinkedHashMap<>(); //TODO: this leaks memory (it's never drained)
    public final Water water;
    private final List<Edge> allEdges = new ArrayList<>();
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
    protected Region decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        Region region = new Region(new ChunkPos(tileX, tileZ), this.water);
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

            for (Edge e : this.allEdges) {
                this.relevantChunks(lowX, lowZ, highX, highZ, e);
            }
            this.allEdges.clear();
        }

        return region;
    }

    public ChunkPos getRegion(double lon, double lat) {
        return new ChunkPos((int) Math.floor(lon / TILE_SIZE), (int) Math.floor(lat / TILE_SIZE));
    }

    public Set<Edge> chunkStructures(int x, int z) {
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

    public Region regionCache(double[] corner) {
        //bound check
        if (!(corner[0] >= -180 && corner[0] <= 180 && corner[1] >= -80 && corner[1] <= 80)) {
            return null;
        }

        ChunkPos coord = this.getRegion(corner[0], corner[1]);
        return this.getTile(coord.x, coord.z);
    }

    private void doGson(InputStream is, Region region) throws IOException {
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
                    Type type = Type.ROAD;

                    if (waterway != null) {
                        type = Type.STREAM;
                        if ("river".equals(waterway) || "canal".equals(waterway)) {
                            type = Type.RIVER;
                        }

                    }

                    if (building != null) {
                        type = Type.BUILDING;
                    }

                    if ("yes".equals(istunnel)) {
                        attributes = Attributes.ISTUNNEL;
                    } else if ("yes".equals(isbridge)) {
                        attributes = Attributes.ISBRIDGE;
                    } else {
                        // totally skip classification if it's a tunnel or bridge. this should make it more efficient.
                        if (highway != null && attributes == Attributes.NONE) {
                            switch (highway) {
                                case "motorway":
                                    type = Type.FREEWAY;
                                    break;
                                case "trunk":
                                    type = Type.LIMITEDACCESS;
                                    break;
                                case "motorway_link":
                                case "trunk_link":
                                    type = Type.INTERCHANGE;
                                    break;
                                case "secondary":
                                    type = Type.SIDE;
                                    break;
                                case "primary":
                                case "raceway":
                                    type = Type.MAIN;
                                    break;
                                case "tertiary":
                                case "residential":
                                    type = Type.MINOR;
                                    break;
                                default:
                                    if ("primary_link".equals(highway) ||
                                        "secondary_link".equals(highway) ||
                                        "living_street".equals(highway) ||
                                        "bus_guideway".equals(highway) ||
                                        "service".equals(highway) ||
                                        "unclassified".equals(highway)) {
                                        type = Type.SIDE;
                                    }
                                    break;
                            }
                        }
                    }
                    //get lane number (default is 2)
                    String slanes = elem.tags.get("lanes");
                    String slayer = elem.tags.get("layers");
                    byte lanes = 2;
                    byte layer = 1;

                    if (slayer != null) {
                        try {
                            layer = Byte.parseByte(slayer);
                        } catch (NumberFormatException e) {
                            // default to layer 1 if bad format
                        }
                    }

                    if (slanes != null) {
                        try {
                            lanes = Byte.parseByte(slanes);
                        } catch (NumberFormatException e) {
                        } //default to 2, if bad format
                    }

                    //prevent super high # of lanes to prevent ridiculous results (prly a mistake if its this high anyways)
                    if (lanes > 8) {
                        lanes = 8;
                    }

                    // an interchange that doesn't have any lane tag should be defaulted to 2 lanes
                    if (lanes < 2 && type == Type.INTERCHANGE) {
                        lanes = 2;
                    }

                    // upgrade road type if many lanes (and the road was important enough to include a lanes tag)
                    if (lanes > 2 && type == Type.MINOR) {
                        type = Type.MAIN;
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
                                this.addWay(way, Type.BUILDING, (byte) 1, region, Attributes.NONE, (byte) 0);
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

    void addWay(Element elem, Type type, byte lanes, Region region, Attributes attributes, byte layer) {
        double[] lastProj = null;
        if (elem.geometry != null) {
            for (Geometry geom : elem.geometry) {
                if (geom == null) {
                    lastProj = null;
                } else {
                    double[] proj = this.projection.fromGeo(geom.lon, geom.lat);

                    if (lastProj != null) { //register as a road edge
                        this.allEdges.add(new Edge(lastProj[0], lastProj[1], proj[0], proj[1], type, lanes, region, attributes, layer));
                    }

                    lastProj = proj;
                }
            }
        }
    }

    Geometry waterway(Element way, long id, Region region, Geometry last) {
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

    private void relevantChunks(int lowX, int lowZ, int highX, int highZ, Edge edge) {
        ChunkPos start = new ChunkPos((int) Math.floor(edge.slon / CHUNK_SIZE), (int) Math.floor(edge.slat / CHUNK_SIZE));
        ChunkPos end = new ChunkPos((int) Math.floor(edge.elon / CHUNK_SIZE), (int) Math.floor(edge.elat / CHUNK_SIZE));

        double startx = edge.slon;
        double endx = edge.elon;

        if (startx > endx) {
            ChunkPos tmp = start;
            start = end;
            end = tmp;
            startx = endx;
            endx = edge.slon;
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

            for (int y = Math.max(from, lowZ); y <= to && y < highZ; y++) {
                this.assoiateWithChunk(new ChunkPos(x, y), edge);
            }
        }
    }

    private void assoiateWithChunk(ChunkPos c, Edge edge) {
        Set<Edge> list = this.chunks.get(c);
        if (list == null) {
            list = new HashSet<>();
            this.chunks.put(c, list);
        }
        list.add(edge);
    }

    public enum Type {
        IGNORE, ROAD, MINOR, SIDE, MAIN, INTERCHANGE, LIMITEDACCESS, FREEWAY, STREAM, RIVER, BUILDING, RAIL
        // ranges from minor to freeway for roads, use road if not known
    }

    public enum Attributes {
        ISBRIDGE, ISTUNNEL, NONE
    }

    public enum EType {
        invalid, node, way, relation, area
    }

    public static class Edge {
        public Type type;
        public double slat;
        public double slon;
        public double elat;
        public double elon;
        public Attributes attribute;
        public byte layer_number;
        public double slope;
        public double offset;

        public byte lanes;

        Region region;

        private Edge(double slon, double slat, double elon, double elat, Type type, byte lanes, Region region, Attributes att, byte ly) {
            //slope must not be infinity, slight inaccuracy shouldn't even be noticible unless you go looking for it
            double dif = elon - slon;
            if (-NOTHING <= dif && dif <= NOTHING) {
                if (dif < 0) {
                    elon -= NOTHING;
                } else {
                    elon += NOTHING;
                }
            }

            this.slat = slat;
            this.slon = slon;
            this.elat = elat;
            this.elon = elon;
            this.type = type;
            this.attribute = att;
            this.lanes = lanes;
            this.region = region;
            this.layer_number = ly;

            this.slope = (elat - slat) / (elon - slon);
            this.offset = slat - this.slope * slon;
        }

        public int hashCode() {
            return (int) ((this.slon * 79399) + (this.slat * 100000) + (this.elat * 13467) + (this.elon * 103466));
        }

        public boolean equals(Object o) {
            Edge e = (Edge) o;
            return e.slat == this.slat && e.slon == this.slon && e.elat == this.elat && e.elon == e.elon;
        }

        public String toString() {
            return "(" + this.slat + ", " + this.slon + ',' + this.elat + ',' + this.elon + ')';
        }
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