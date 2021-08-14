package net.buildtheearth.terraplusplus.dataset.geojson.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.Dataset;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraplusplus.dataset.geojson.object.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@JsonSerialize
@Getter(onMethod_ = { @JsonGetter })
public abstract class AbstractReferenceResolvingGeoJsonDataset<V> extends Dataset<String, V> {
    protected static boolean areAnyObjectsReferences(@NonNull GeoJsonObject[] objects) {
        for (GeoJsonObject object : objects) {
            if (object instanceof Reference) {
                return true;
            }
        }
        return false;
    }

    protected final ParsingGeoJsonDataset delegate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AbstractReferenceResolvingGeoJsonDataset(
            @JsonProperty(value = "delegate", required = true) @NonNull ParsingGeoJsonDataset delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<V> load(@NonNull String key) throws Exception {
        return this.delegate.getAsync(key).thenCompose(objects -> {
            if (objects == null) { //404 not found
                return CompletableFuture.completedFuture(this.translate(Stream.empty()));
            }

            if (!areAnyObjectsReferences(objects)) { //none of the objects are references, so there's nothing to be resolved!
                return CompletableFuture.completedFuture(this.translate(Arrays.stream(objects)));
            }

            //resolve references asynchronously
            List<GeoJsonObject> nonReferenceObjects = new ArrayList<>();
            List<CompletableFuture<V>> referenceFutures = new ArrayList<>();
            for (GeoJsonObject object : objects) {
                if (object instanceof Reference) {
                    String location = ((Reference) object).location();
                    String prefix = key.substring(0, key.indexOf('/') + 1); //TODO: this is gross
                    referenceFutures.add(this.getAsync(prefix + location));
                } else {
                    nonReferenceObjects.add(object);
                }
            }

            if (!nonReferenceObjects.isEmpty()) {
                referenceFutures.add(CompletableFuture.completedFuture(this.translate(nonReferenceObjects.stream())));
            }

            CompletableFuture<V>[] packedReferenceFutures = uncheckedCast(referenceFutures.toArray(new CompletableFuture[0]));
            return CompletableFuture.allOf(packedReferenceFutures).thenApply(unused ->
                    this.merge(Arrays.stream(packedReferenceFutures).map(CompletableFuture::join)));
        });
    }

    protected abstract V translate(@NonNull Stream<GeoJsonObject> inputs);

    protected abstract V merge(@NonNull Stream<V> inputs);
}
