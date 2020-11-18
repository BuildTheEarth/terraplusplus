package io.github.terra121.control;

import io.github.terra121.control.fragments.FragmentManager;
import io.github.terra121.control.fragments.admin.AdminOverpassFragment;
import io.github.terra121.control.fragments.terra.TerraConvertFragment;
import io.github.terra121.control.fragments.terra.TerraDistortionFragment;
import io.github.terra121.control.fragments.terra.TerraEnvironmentFragment;
import io.github.terra121.control.fragments.terra.TerraInfoFragment;
import io.github.terra121.control.fragments.terra.TerraInvertWaterFragment;
import io.github.terra121.control.fragments.terra.TerraOsmFragment;
import io.github.terra121.control.fragments.terra.TerraWhereFragment;
import io.github.terra121.control.fragments.terra.TerraWorldFragment;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class TerraAdminCommand extends FragmentManager {

    public TerraAdminCommand() {
        setTitle(TranslateUtil.translate("terra121.commands.terraadmin.title"));
        setCommandBase("terraadmin");
        registerCommandFragment(new AdminOverpassFragment());
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("ta");
        return aliases;
    }

    @Override
    public String getName() {
        return "terraadmin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "terra121.commands.terraadmin.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        executeFragment(server, sender, args);
    }
}
