package io.github.terra121.control;

import io.github.terra121.TerraConstants;
import io.github.terra121.control.fragments.FragmentManager;
import io.github.terra121.control.fragments.terra.TerraConvertFragment;
import io.github.terra121.control.fragments.terra.TerraDistortionFragment;
import io.github.terra121.control.fragments.terra.TerraEnvironmentFragment;
import io.github.terra121.control.fragments.terra.TerraInfoFragment;
import io.github.terra121.control.fragments.terra.TerraOsmFragment;
import io.github.terra121.control.fragments.terra.TerraWhereFragment;
import io.github.terra121.control.fragments.terra.TerraWorldFragment;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class TerraCommand extends FragmentManager {

    public TerraCommand() {
        setTitle(TranslateUtil.translate("terra121.commands.terra.title"));
        setCommandBase("terra");
        registerCommandFragment(new TerraInfoFragment());
        registerCommandFragment(new TerraWhereFragment());
        registerCommandFragment(new TerraWorldFragment());
        registerCommandFragment(new TerraOsmFragment());
        registerCommandFragment(new TerraConvertFragment());
        registerCommandFragment(new TerraEnvironmentFragment());
        registerCommandFragment(new TerraDistortionFragment());
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("t");
        return aliases;
    }

    @Override
    public String getName() {
        return "terra";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "terra121.commands.terra.usage";
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
        if(!hasPermission(TerraConstants.controlCommandNode + "terra", sender)) {
            sender.sendMessage(TerraConstants.TextConstants.getNoPermission());
            return;
        }
        executeFragment(server, sender, args);
    }
}
