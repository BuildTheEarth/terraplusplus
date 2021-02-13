package net.buildtheearth.terraplusplus.control.fragments;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class CommandFragment {
    protected boolean hasPermission(ICommandSender sender) {
        return hasPermission(sender, getPermission());
    }

    protected boolean hasPermission(ICommandSender sender, String permission) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && sender.getEntityWorld().getWorldInfo().areCommandsAllowed()) return true;
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, permission);
        }
        return sender.canUseCommand(2, "");
    }

    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args);

    public abstract String[] getName();

    public abstract String getPurpose();

    public abstract String[] getArguments();

    public abstract String getPermission();
}
