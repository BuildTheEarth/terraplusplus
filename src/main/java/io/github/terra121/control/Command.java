package io.github.terra121.control;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class Command extends CommandBase {
    protected boolean hasPermission(String perm, ICommandSender sender) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && sender.getEntityWorld().getWorldInfo().areCommandsAllowed()) return true;
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, perm);
        }

        return sender.canUseCommand(2, "");

    }

    protected boolean hasAdminPermission(String perm, ICommandSender sender) {
        if(hasPermission(perm, sender)) return true;
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, "terra121.admin");
        }

        return sender.canUseCommand(2, "");

    }
}
