package io.github.terra121.control.fragments;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class CommandFragment implements ICommandFragment {
    protected boolean hasPermission(ICommandSender sender) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && sender.getEntityWorld().getWorldInfo().areCommandsAllowed()) return true;
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, getPermission());
        }

        return sender.canUseCommand(2, "");
    }

    protected boolean hasAdminPermission(ICommandSender sender) {
        if(hasPermission(sender)) return true;
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, "terra121.admin");
        }

        return sender.canUseCommand(2, "");
    }
}
