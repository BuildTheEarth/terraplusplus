package io.github.terra121.dataset.osm;

import com.google.gson.Gson;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.Water;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.*;

public class OpenStreetMap extends TiledDataset<OSMRegion> {
    private static final double CHUNK_SIZE = 16;
    public static final double TILE_SIZE = 1 / 60.0;//250*(360.0/40075000.0);

    public final Water water;
    private final List<Segment> allSegments = new ArrayList<>();
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
    protected String[] urls(int tileX, int tileZ) {
        return TerraConfig.data.overpass;
    }

    @Override
    protected OSMRegion decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        OSMRegion region = new OSMRegion(new ChunkPos(tileX, tileZ), this.water);
        this.doGson(new ByteBufInputStream(data), region);

        region.segments = new BVH<>(this.allSegments);
        this.allSegments.clear();

        return region;
    }

    public ChunkPos getRegion(double lon, double lat) {
        return new ChunkPos((int) Math.floor(lon / TILE_SIZE), (int) Math.floor(lat / TILE_SIZE));
    }

    public Set<Segment> segmentsForChunk(int chunkX, int chunkZ, double padding) {
        Set<Segment> segments = new HashSet<>();
        Set<ChunkPos> processedSegments = new HashSet<>();

        Bounds2d bb = Bounds2d.of(
                Coords.cubeToMinBlock(chunkX) - padding, Coords.cubeToMinBlock(chunkX + 1) + padding,
                Coords.cubeToMinBlock(chunkZ) - padding, Coords.cubeToMinBlock(chunkZ + 1) + padding);

        //it's unfortunate that i have to do it like this rather than simply getting the corners, but there are no guarantees that the
        // projection is linear or almost linear
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                OSMRegion region = this.regionCache(this.projection.toGeo(Coords.cubeToMinBlock(chunkX + max(dx, 0)) + copySign(padding, dx), Coords.cubeToMinBlock(chunkZ + max(dz, 0)) + copySign(padding, dz)));
                if (region != null && processedSegments.add(region.coord)) {
                    region.segments.forEachIntersecting(bb, segments::add);
                }
            }
        }

        return segments;
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
                        this.allSegments.add(new Segment(lastProj[0], lastProj[1], proj[0], proj[1], type, lanes, region, attributes, layer));
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