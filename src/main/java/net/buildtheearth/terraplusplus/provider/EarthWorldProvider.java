package net.buildtheearth.terraplusplus.provider;

import net.buildtheearth.terraplusplus.EarthWorldType;
import net.buildtheearth.terraplusplus.generator.populate.SnowPopulator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;

public class EarthWorldProvider extends WorldProviderSurface {
    protected boolean isEarth;

    @Override
    public void init() {
        super.init();
        this.isEarth = this.world.getWorldInfo().getTerrainType() instanceof EarthWorldType;
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        if (!this.isEarth) {
            return super.canSnowAt(pos, checkLight);
        }

        return SnowPopulator.canSnow(pos, this.world, false);
    }
}
