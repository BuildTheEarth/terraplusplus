package net.buildtheearth.terraplusplus.control;

import net.buildtheearth.terraplusplus.TerraConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class Command extends CommandBase {

    public Command() {
        PermissionAPI.registerNode(TerraConstants.defaultCommandNode + getName(), DefaultPermissionLevel.ALL, "");
    }

    protected boolean hasPermission(ICommandSender sender, String perm) {
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, perm);
        }
        return sender.canUseCommand(2, "");
    }
}
