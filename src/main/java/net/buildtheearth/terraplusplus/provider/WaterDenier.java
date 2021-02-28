package net.buildtheearth.terraplusplus.provider;

import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.generator.EarthBiomeProvider;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.CreateFluidSourceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@UtilityClass
public class WaterDenier {
    @SubscribeEvent
    public void sourceCatch(CreateFluidSourceEvent event) {
        World world = event.getWorld();

        if (world.getBiomeProvider() instanceof EarthBiomeProvider) {
            if (event.getState().getMaterial() != Material.WATER) {
                return;
            }

            BlockPos pos = event.getPos();

            int c = 0;
            c += isSource(world, pos.north());
            c += isSource(world, pos.south());
            c += isSource(world, pos.east());
            c += isSource(world, pos.west());

            if (c < 3) {
                event.setResult(PopulateCubeEvent.Populate.Result.DENY);
            }
        }
    }

    private int isSource(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return (state.getBlock() instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0) ? 1 : 0;
    }
}
