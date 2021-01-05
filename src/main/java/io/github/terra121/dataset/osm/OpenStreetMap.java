package io.github.terra121.dataset.osm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.geojson.GeoJSON;
import io.github.terra121.dataset.geojson.GeoJSONObject;
import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.Point;
import io.github.terra121.dataset.geojson.object.Reference;
import io.github.terra121.dataset.impl.Water;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.dataset.osm.segment.SegmentType;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.daporkchop.lib.common.function.PFunctions;
import net.minecraft.util.math.ChunkPos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

public class OpenStreetMap extends TiledDataset<OSMRegion> {
    protected static final String TILE_SUFFIX = "tile/${x}/${z}.json";

    public static final double TILE_SIZE = 1 / 64.0;

    protected final GeographicProjection earthProjection;

    protected final LoadingCache<String, CompletableFuture<OSMBlob>> referencedBlobs = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(CacheLoader.from(location -> {
                //suffix urls with location
                String[] urls = Arrays.stream(TerraConfig.data.openstreetmap).map(s -> s + location).toArray(String[]::new);

                return Http.getFirst(urls, this::parseGeoJSON).thenCompose(PFunctions.identity());
            }));

    public final Water water;
    private final boolean doRoad;
    private final boolean doWater;
    private final boolean doBuildings;

    public OpenStreetMap(@NonNull GeographicProjection earthProjection, boolean doRoad, boolean doWater, boolean doBuildings) {
        super(new EquirectangularProjection(), TILE_SIZE);

        this.earthProjection = earthProjection;

        try {
            this.water = new Water(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.doRoad = doRoad;
        this.doWater = doWater;
        this.doBuildings = doBuildings;
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return Arrays.stream(TerraConfig.data.openstreetmap).map(s -> s + TILE_SUFFIX).toArray(String[]::new);
    }

    @Override
    @Deprecated
    protected OSMRegion decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        throw new UnsupportedOperationException("decode"); //this method shouldn't be being used
    }

    @Override
    @Deprecated
    public CompletableFuture<OSMRegion> load(@NonNull ChunkPos pos) throws Exception {
        String location = Http.formatUrl(ImmutableMap.of("x", String.valueOf(pos.x), "z", String.valueOf(pos.z)), TILE_SUFFIX);

        return this.referencedBlobs.getUnchecked(location).thenApply(blob -> this.toRegion(pos, blob));
    }

    protected CompletableFuture<OSMBlob> parseGeoJSON(@NonNull ByteBuf json) throws IOException {
        GeoJSONObject[] objects; //parse each line as a GeoJSON object
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteBufInputStream(json)))) {
            objects = reader.lines().map(GeoJSON::parse).toArray(GeoJSONObject[]::new);
        }

        if (Arrays.stream(objects).anyMatch(o -> o instanceof Reference)) { //resolve references
            List<CompletableFuture<OSMBlob>> referencedObjectFutures = new ArrayList<>();

            //add all non-reference objects as a single stream at once to make iteration faster
            referencedObjectFutures.add(CompletableFuture.completedFuture(OSMBlob.fromGeoJSON(
                    this.earthProjection, Arrays.stream(objects).filter(o -> !(o instanceof Reference)).toArray(GeoJSONObject[]::new))));

            for (GeoJSONObject object : objects) {
                if (object instanceof Reference) {
                    //suffix urls with location
                    String location = ((Reference) object).location();
                    String[] urls = Arrays.stream(TerraConfig.data.openstreetmap).map(s -> s + location).toArray(String[]::new);

                    //actually send request
                    referencedObjectFutures.add(Http.getFirst(urls, this::parseGeoJSON).thenCompose(PFunctions.identity()));
                }
            }

            return CompletableFuture.allOf(referencedObjectFutures.toArray(new CompletableFuture[0]))
                    .thenApply(unused -> OSMBlob.merge(referencedObjectFutures.stream().map(CompletableFuture::join).toArray(OSMBlob[]::new)));
        }

        return CompletableFuture.completedFuture(OSMBlob.fromGeoJSON(this.earthProjection, objects));
    }

    protected OSMRegion toRegion(@NonNull ChunkPos pos, @NonNull OSMBlob blob) {
        Stream<OSMSegment> segments = Arrays.stream(blob.segments()).filter(s -> SegmentType.USABLE_TYPES.contains(s.type));
        Stream<OSMPolygon> polygons = Arrays.stream(blob.polygons());

        if (!this.doBuildings) {
            segments = segments.filter(s -> SegmentType.NOT_BUILDING_TYPES.contains(s.type));
        }
        if (!this.doWater) {
            segments = segments.filter(s -> SegmentType.NOT_WATER_TYPES.contains(s.type));
        }
        if (!this.doRoad) {
            segments = segments.filter(s -> SegmentType.NOT_ROAD_TYPES.contains(s.type));
        }

        OSMRegion region = new OSMRegion(pos, this.water, new BVH<>(segments.collect(Collectors.toList())), new BVH<>(polygons.collect(Collectors.toList())));

        Set<Long> ground;
        if (this.doWater) {
            ground = new HashSet<>();

            long id = 2400000000L; //not sure what this constant is for, but it'll be removed later anyway so idc
            for (LineString string : blob.waterEdges()) {
                Point last = null;
                for (Point point : string.points()) {
                    if (last != null) {
                        region.addWaterEdge(last.lon(), last.lat(), point.lon(), point.lat(), id);
                    }
                    last = point;
                }
                id++;
            }

            if (this.water.grounding.state(pos.x, pos.z) == 0) {
                ground.add(-1L);
            }
        } else {
            ground = Collections.emptySet();
        }
        region.renderWater(ground);

        return region;
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
}
