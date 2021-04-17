package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.IEarthAsyncPipelineStep;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeIdResolver(IEarthDataBaker.TypeIdResolver.class)
@JsonDeserialize
public interface IEarthDataBaker<D> extends IEarthAsyncPipelineStep<D, CachedChunkData, CachedChunkData.Builder> {
    /**
     * @return this {@link IEarthDataBaker}'s type ID
     */
    default String typeId() {
        String typeId = GlobalParseRegistries.GENERATOR_SETTINGS_DATA_BAKER.inverse().get(this.getClass());
        checkState(typeId != null, "unknown IEarthBiomeFilter implementation: %s", this.getClass());
        return typeId;
    }

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<IEarthDataBaker> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.GENERATOR_SETTINGS_DATA_BAKER);
        }
    }
}
