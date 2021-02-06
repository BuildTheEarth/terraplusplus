package io.github.terra121.dataset.osm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.dataset.TiledHttpDataset;
import io.github.terra121.dataset.osm.config.OSMMapper;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.dataset.geojson.GeoJson;
import io.github.terra121.dataset.geojson.GeoJsonObject;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.Point;
import io.github.terra121.dataset.geojson.object.Feature;
import io.github.terra121.dataset.geojson.object.Reference;
import io.github.terra121.generator.EarthGeneratorSettings;
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
import lombok.SneakyThrows;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.util.math.ChunkPos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.daporkchop.lib.common.util.PorkUtil.*;

public class OpenStreetMap extends TiledHttpDataset<OSMRegion> {
    protected static final Function<CompletableFuture<VectorGeometry[]>, CompletableFuture<VectorGeometry[]>> COMPOSE_FUNCTION =
            blob -> blob != null ? blob : CompletableFuture.completedFuture(null);

    protected static final String TILE_SUFFIX = "tile/${x}/${z}.json";

    public static final double TILE_SIZE = 1 / 64.0;

    protected final GeographicProjection earthProjection;
    protected final OSMMapper<Geometry> mapper;
    protected final LoadingCache<String, CompletableFuture<VectorGeometry[]>> referencedBlobs = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(CacheLoader.from(location -> {
                //suffix urls with location
                String[] urls = Arrays.stream(TerraConfig.openstreetmap.servers).map(s -> s + location).toArray(String[]::new);

                return Http.getFirst(urls, this::parseGeoJSON).thenCompose(COMPOSE_FUNCTION);
            }));

    @SneakyThrows(IOException.class)
    public OpenStreetMap(@NonNull EarthGeneratorSettings settings) {
        super(new EquirectangularProjection(), TILE_SIZE);

        this.earthProjection = settings.projection();

        this.mapper = OSMMapper.load(OpenStreetMap.class.getResourceAsStream("osm.json5"));
    }

    @Override
    protected String[] urls(int tileX, int tileZ) {
        return Arrays.stream(TerraConfig.openstreetmap.servers).map(s -> s + TILE_SUFFIX).toArray(String[]::new);
    }

    @Override
    @Deprecated
    protected OSMRegion decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        throw new UnsupportedOperationException("decode"); //this method shouldn't be being used
    }

    @Override
    public CompletableFuture<OSMRegion> load(@NonNull ChunkPos pos) throws Exception {
        String location = Http.formatUrl(ImmutableMap.of("x", String.valueOf(pos.x), "z", String.valueOf(pos.z)), TILE_SUFFIX);

        return this.referencedBlobs.getUnchecked(location).thenApply(blob -> this.toRegion(pos, blob));
    }

    protected CompletableFuture<VectorGeometry[]> parseGeoJSON(@NonNull ByteBuf json) throws IOException {
        GeoJsonObject[] objects; //parse each line as a GeoJSON object
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteBufInputStream(json)))) {
            objects = reader.lines().map(GeoJson::parse).toArray(GeoJsonObject[]::new);
        }

        CompletableFuture<VectorGeometry[]> nonReferenceFuture = CompletableFuture.completedFuture(Arrays.stream(objects)
                .flatMap(o -> this.convertToElements(null, Collections.emptyMap(), o))
                .toArray(VectorGeometry[]::new));

        if (Arrays.stream(objects).anyMatch(o -> o instanceof Reference)) { //resolve references
            List<CompletableFuture<VectorGeometry[]>> referencedObjectFutures = new ArrayList<>();

            referencedObjectFutures.add(nonReferenceFuture);

            for (GeoJsonObject object : objects) {
                if (object instanceof Reference) {
                    //get from cache or send request
                    referencedObjectFutures.add(this.referencedBlobs.getUnchecked(((Reference) object).location()));
                }
            }

            return CompletableFuture.allOf(referencedObjectFutures.toArray(new CompletableFuture[0]))
                    .thenApply(unused -> referencedObjectFutures.stream().map(CompletableFuture::join).flatMap(Arrays::stream).toArray(VectorGeometry[]::new));
        }

        return nonReferenceFuture;
    }

    protected Stream<VectorGeometry> convertToElements(String id, @NonNull Map<String, String> tags, @NonNull GeoJsonObject object) {
        if (object instanceof Iterable) {
            //recursively process all child elements
            return StreamSupport.stream(PorkUtil.<Iterable<? extends GeoJsonObject>>uncheckedCast(object).spliterator(), false)
                    .flatMap(child -> this.convertToElements(id, tags, child));
        } else if (object instanceof Feature) {
            //process child using properties from feature
            Feature feature = (Feature) object;
            return this.convertToElements(feature.id() != null ? feature.id() : id, feature.properties() != null ? feature.properties() : tags, feature.geometry());
        } else if (object instanceof Point) { //TODO: we currently can't handle points
            return Stream.empty();
        } else if (object instanceof Reference) { //ignore references, they'll be resolved later asynchronously
            return Stream.empty();
        } else {
            try {
                Geometry geometry = (Geometry) object;
                Collection<VectorGeometry> elements = this.mapper.apply(id, tags, geometry, geometry.project(this.earthProjection::fromGeo));
                return elements != null ? elements.stream() : Stream.empty();
            } catch (OutOfProjectionBoundsException e) {//skip element
                return Stream.empty();
            }
        }
    }

    protected OSMRegion toRegion(@NonNull ChunkPos pos, VectorGeometry[] elements) {
        if (elements == null) {
            elements = new VectorGeometry[0];
        }

        return new OSMRegion(pos, BVH.of(elements));
    }

    public CompletableFuture<OSMRegion[]> getRegionsAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        Bounds2d localBounds = bounds.fromGeo(this.projection).axisAlign();
        CompletableFuture<OSMRegion>[] futures = uncheckedCast(Arrays.stream(localBounds.toTiles(TILE_SIZE))
                .map(this::getAsync)
                .toArray(CompletableFuture[]::new));

        return CompletableFuture.allOf(futures)
                .thenApplyAsync(unused -> Arrays.stream(futures).map(CompletableFuture::join).toArray(OSMRegion[]::new));
    }
}
