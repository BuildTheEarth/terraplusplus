package net.buildtheearth.terraplusplus.control;

import com.google.common.collect.Lists;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.control.fragments.FragmentManager;
import net.buildtheearth.terraplusplus.control.fragments.terra.TerraConvertFragment;
import net.buildtheearth.terraplusplus.control.fragments.terra.TerraDistortionFragment;
import net.buildtheearth.terraplusplus.control.fragments.terra.TerraInfoFragment;
import net.buildtheearth.terraplusplus.control.fragments.terra.TerraWhereFragment;
import net.buildtheearth.terraplusplus.control.fragments.terra.TerraWorldFragment;
import net.buildtheearth.terraplusplus.util.ChatUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class TerraCommand extends FragmentManager {

    public TerraCommand() {
        super("terra");
        register(new TerraInfoFragment());
        register(new TerraWhereFragment());
        register(new TerraWorldFragment());
        register(new TerraConvertFragment());
        register(new TerraDistortionFragment());
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("t");
    }

    @Override
    public String getName() {
        return "terra";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TerraConstants.controlCommandNode + "terra.usage";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(!hasPermission(sender, TerraConstants.controlCommandNode + "terra")) {
            sender.sendMessage(ChatUtil.getNoPermission());
            return;
        }
        executeFragment(server, sender, args);
    }
}
