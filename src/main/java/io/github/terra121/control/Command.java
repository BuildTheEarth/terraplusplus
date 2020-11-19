package io.github.terra121.control;

import io.github.terra121.TerraConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class Command extends CommandBase {

    public Command() {
        PermissionAPI.registerNode(TerraConstants.defaultCommandNode + getName(), DefaultPermissionLevel.ALL, "");
    }

    protected boolean hasPermission(String perm, ICommandSender sender) {
        if(perm == null) perm = TerraConstants.adminCommandNode;
        if (sender instanceof EntityPlayer) {
            if(PermissionAPI.hasPermission((EntityPlayer) sender, TerraConstants.adminCommandNode)) return true;
            return PermissionAPI.hasPermission((EntityPlayer) sender, perm);
        }

        return sender.canUseCommand(2, "");

    }
}
