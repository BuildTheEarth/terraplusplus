package net.buildtheearth.terraplusplus.asm.world.storage;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.world.storage.IWorldInfoAccess;
import net.buildtheearth.terraplusplus.EarthWorldType;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author DaPorkchop_
 */
@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler {
    @Shadow
    @Final
    private File worldDirectory;

    @Inject(method = "Lnet/minecraft/world/storage/SaveHandler;loadWorldInfo()Lnet/minecraft/world/storage/WorldInfo;",
            at = @At("RETURN"),
            require = 1)
    private void terraplusplus_loadWorldInfo_loadSettingsFromFile(CallbackInfoReturnable<WorldInfo> ci) {
        if (ci.getReturnValue() == null || !(ci.getReturnValue().getTerrainType() instanceof EarthWorldType)) {
            return;
        }

        Path settingsFile = EarthGeneratorSettings.settingsFile(this.worldDirectory.toPath());
        if (Files.exists(settingsFile)) {
            ((IWorldInfoAccess) ci.getReturnValue()).setGeneratorOptions(EarthGeneratorSettings.readSettings(settingsFile));
        } else {
            EarthGeneratorSettings.writeSettings(settingsFile, ci.getReturnValue().getGeneratorOptions());
        }
    }

    @Inject(method = "Lnet/minecraft/world/storage/SaveHandler;saveWorldInfoWithPlayer(Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/nbt/NBTTagCompound;)V",
            at = @At("RETURN"),
            require = 1)
    private void terraplusplus_saveWorldInfoWithPlayer_writeSettingsToFile(WorldInfo worldInfo, NBTTagCompound nbt, CallbackInfo ci) {
        if (!(worldInfo.getTerrainType() instanceof EarthWorldType)) {
            return;
        }

        EarthGeneratorSettings.writeSettings(EarthGeneratorSettings.settingsFile(this.worldDirectory.toPath()), worldInfo.getGeneratorOptions());
    }
}
