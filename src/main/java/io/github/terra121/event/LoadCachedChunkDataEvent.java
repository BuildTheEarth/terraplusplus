package io.github.terra121.event;

import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.ChunkDataLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.concurrent.CompletableFuture;

/**
 * Fired on {@link MinecraftForge#TERRAIN_GEN_BUS} when {@link ChunkDataLoader} is loading data for a chunk column.
 * <p>
 * Allows registration of custom data to be attached to the completed {@link CachedChunkData}.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class LoadCachedChunkDataEvent extends AbstractCustomRegistrationEvent<CompletableFuture<?>> {
    @NonNull
    protected final ChunkPos pos;
    @NonNull
    protected final GeneratorDatasets datasets;
}
