package net.buildtheearth.terraplusplus.control.fragments;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public interface ICommandFragment {
    void execute(MinecraftServer server, ICommandSender sender, String[] args);

    String[] getName();

    String getPurpose();

    String[] getArguments();

    String getPermission();
}
