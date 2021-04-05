package net.buildtheearth.terraplusplus.provider;

import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldServer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.DecorateCubeBiomeEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerraConstants.MODID)
@UtilityClass
public class GenerationEventDenier {
    @SubscribeEvent
    public void populateCatcher(PopulateCubeEvent.Populate event) {
        ICubeGenerator generator = event.getGenerator();
        if (generator instanceof EarthGenerator && ((EarthGenerator) generator).settings.skipChunkPopulation().contains(event.getType())) {
            event.setResult(PopulateCubeEvent.Populate.Result.DENY);
        }
    }

    @SubscribeEvent
    public void decorateCatcher(DecorateCubeBiomeEvent.Decorate event) {
        World world = event.getWorld();
        if (world instanceof ICubicWorldServer) {
            ICubeGenerator generator = ((ICubicWorldServer) world).getCubeGenerator();
            if (generator instanceof EarthGenerator && ((EarthGenerator) generator).settings.skipBiomeDecoration().contains(event.getType())) {
                event.setResult(PopulateCubeEvent.Populate.Result.DENY);
            }
        }
    }
}
