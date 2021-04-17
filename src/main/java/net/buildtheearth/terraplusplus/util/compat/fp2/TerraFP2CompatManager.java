package net.buildtheearth.terraplusplus.util.compat.fp2;

import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldServer;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.daporkchop.fp2.mode.heightmap.event.RegisterRoughHeightmapGeneratorsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class TerraFP2CompatManager {
    @SubscribeEvent
    public void registerHeightmapRoughGenerator(RegisterRoughHeightmapGeneratorsEvent event) {
        event.registry().addLast("terra++", world -> world instanceof ICubicWorldServer && ((ICubicWorldServer) world).getCubeGenerator() instanceof EarthGenerator
                ? new TerraHeightmapGeneratorRough(world)
                : null);
    }
}
