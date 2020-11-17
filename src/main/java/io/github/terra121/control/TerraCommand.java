package io.github.terra121.control;

import io.github.terra121.control.fragments.FragmentManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class TerraCommand extends FragmentManager {

    public TerraCommand() {
        setTitle("terra121.commands.terra.title");
        setCommandBase("terra");
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        executeFragment(server, sender, args);
    }
}
