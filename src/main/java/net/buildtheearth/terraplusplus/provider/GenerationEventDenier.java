package net.buildtheearth.terraplusplus.provider;

import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.DecorateCubeBiomeEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.generator.EarthBiomeProvider;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import lombok.experimental.UtilityClass;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//deny default tree and snow event because we have some of those already
@Mod.EventBusSubscriber(modid = TerraConstants.MODID)
@UtilityClass
public class GenerationEventDenier {
    @SubscribeEvent
    public void populateCatcher(PopulateCubeEvent.Populate event) {
        if (event.getType() == PopulateChunkEvent.Populate.EventType.ICE && event.getGenerator() instanceof EarthGenerator) {
            event.setResult(PopulateCubeEvent.Populate.Result.DENY);
        }
    }

    @SubscribeEvent
    public void decorateCatcher(DecorateCubeBiomeEvent.Decorate event) {
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.TREE && event.getWorld().getBiomeProvider() instanceof EarthBiomeProvider) {
            event.setResult(PopulateCubeEvent.Populate.Result.DENY);
        }
    }
}
