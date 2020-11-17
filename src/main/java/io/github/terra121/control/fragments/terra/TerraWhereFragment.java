package io.github.terra121.control.fragments.terra;

import io.github.terra121.control.fragments.ICommandFragment;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TerraWhereFragment implements ICommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {

    }

    @Override
    public String[] getName() {
        return new String[]{"where", "ou"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.where.purpose");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"[player]"};
    }
}
