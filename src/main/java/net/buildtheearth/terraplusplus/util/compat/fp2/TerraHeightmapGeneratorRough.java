package net.buildtheearth.terraplusplus.util.compat.fp2;

import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldServer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.CliffReplacer;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.daporkchop.fp2.compat.vanilla.FastRegistry;
import net.daporkchop.fp2.mode.heightmap.HeightmapData;
import net.daporkchop.fp2.mode.heightmap.HeightmapPos;
import net.daporkchop.fp2.mode.heightmap.HeightmapTile;
import net.daporkchop.fp2.mode.heightmap.server.gen.rough.AbstractRoughHeightmapGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.WorldServer;

import java.util.function.Supplier;

import static net.daporkchop.fp2.mode.heightmap.HeightmapConstants.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public class TerraHeightmapGeneratorRough extends AbstractRoughHeightmapGenerator {
    protected final EarthGenerator generator;

    public TerraHeightmapGeneratorRough(@NonNull WorldServer world) {
        super(world);

        this.generator = (EarthGenerator) ((ICubicWorldServer) world).getCubeGenerator();
    }

    @Override
    public boolean supportsLowResolution() {
        return true;
    }

    @Override
    public void generate(@NonNull HeightmapPos pos, @NonNull HeightmapTile tile) {
        Supplier<IBlockState> fill = this.generator.settings.terrainSettings().fill();
        Supplier<IBlockState> water = this.generator.settings.terrainSettings().water();
        Supplier<IBlockState> surface = this.generator.settings.terrainSettings().surface();
        Supplier<IBlockState> top = this.generator.settings.terrainSettings().top();

        CachedChunkData cached = this.generator.cache.getUnchecked(new TilePos(pos.x(), pos.z(), pos.level())).join();

        HeightmapData data = new HeightmapData();

        HeightmapData waterData = new HeightmapData();
        waterData.light = 15 << 4;
        waterData.height_int = this.generator.settings.customCubic().waterLevel - 1;
        waterData.height_frac = 224; //256 * 7/8
        waterData.secondaryConnection = 1;
        waterData.state = water.get();

        int blockX = pos.blockX();
        int blockZ = pos.blockZ();
        int level = pos.level();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int biome = cached.biome(x, z) & 0xFF;
                data.biome = waterData.biome = FastRegistry.getBiome(biome);

                int groundHeight = data.height_int = cached.groundHeight(x, z);
                int waterHeight = cached.waterHeight(x, z);
                waterData.height_int = (groundHeight <= waterHeight ? waterHeight : waterHeight - (1 << level)) - 1;

                //horizontal density change is calculated using the top height rather than the ground height
                int topHeight = cached.surfaceHeight(x, z);
                double dx = x == 15 ? topHeight - cached.surfaceHeight(x - 1, z) : cached.surfaceHeight(x + 1, z) - topHeight;
                double dz = z == 15 ? topHeight - cached.surfaceHeight(x, z - 1) : cached.surfaceHeight(x, z + 1) - topHeight;

                IBlockState state = cached.surfaceBlock(x, z);
                if (state != null) {
                    data.height_int = topHeight;
                } else {
                    if (this.generator.settings.terrainSettings().useCwgReplacers()) {
                        state = fill.get();
                        for (IBiomeBlockReplacer replacer : this.generator.biomeBlockReplacers[biome]) {
                            state = replacer.getReplacedBlock(state, blockX + (x << level), groundHeight, blockZ + (z << level), dx, -1.0d, dz, 0.0d);
                        }

                        //calling this explicitly increases the likelihood of JIT inlining it
                        //(for reference: previously, CliffReplacer was manually added to each biome as the last replacer)
                        //state = CliffReplacer.INSTANCE.getReplacedBlock(state, blockX + (x << level), groundHeight, blockZ + (z << level), dx, -1.0d, dz, 1.0d);

                        if (groundHeight < waterHeight && state == top.get()) { //hacky workaround for underwater grass
                            state = surface.get();
                        }
                    } else {
                        state = top.get();
                    }
                }
                data.state = state;

                data.light = (15 - clamp(waterHeight - groundHeight, 0, 5) * 3) << 4;

                tile.setLayer(x, z, DEFAULT_LAYER, data);
            }
        }
    }
}
