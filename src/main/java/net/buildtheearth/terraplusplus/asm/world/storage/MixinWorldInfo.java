package net.buildtheearth.terraplusplus.asm.world.storage;

import net.buildtheearth.terraplusplus.EarthWorldType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author DaPorkchop_
 */
@Mixin(WorldInfo.class)
public abstract class MixinWorldInfo {
    @Shadow
    private WorldType terrainType;

    @Shadow
    private String generatorOptions;

    @Redirect(method = "Lnet/minecraft/world/storage/WorldInfo;updateTagCompound(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/nbt/NBTTagCompound;)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/storage/WorldInfo;generatorOptions:Ljava/lang/String;"),
            require = 1, allow = 1)
    private String terraplusplus_updateTagCompound_dontStoreGeneratorOptionsToLevelDat(WorldInfo _this) {
        if (this.terrainType instanceof EarthWorldType) {
            return "";
        } else {
            return this.generatorOptions;
        }
    }
}
