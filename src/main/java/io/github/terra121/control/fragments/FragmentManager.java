package io.github.terra121.control.fragments;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public abstract class FragmentManager extends CommandBase {

    private List<ICommandFragment> commandFragments = new ArrayList<>();
    private String title = "";
    private String commandBase = "";

    protected void registerCommandFragment(ICommandFragment c) {
        commandFragments.add(c);
    }

    protected void setTitle(String t) {
        this.title = " "+t;
    }

    protected void setCommandBase(String b) {
        this.commandBase = "/"+b+" ";
    }

    protected void executeFragment(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 0) {
            ArrayList<String> dataList = new ArrayList<>();
            for (int x = 1; x < args.length; x++) dataList.add(args[x]);

            String[] data = dataList.toArray(new String[dataList.size()]);
            for (ICommandFragment f : commandFragments) {
                for(String c : f.getName()) {
                    if (c.equalsIgnoreCase(args[0])) {
                        f.execute(server, sender, data);
                        return;
                    }
                }

            }
        }
        displayCommands(sender, args);
    }

    private void displayCommands(ICommandSender sender, String[] args) {
        sender.sendMessage(new TextComponentString(title + ":").setStyle(new Style().setColor(TextFormatting.GRAY)));
        for(ICommandFragment f : commandFragments) {

            ITextComponent message = new TextComponentString(commandBase).setStyle(new Style().setColor(TextFormatting.YELLOW));
            message.appendSibling(new TextComponentString(f.getName()[0] + " ").setStyle(new Style().setColor(TextFormatting.GREEN)));
            if(f.getArguments() != null) {
                for(int x = 0; x < f.getArguments().length; x++) {
                    String argument = f.getArguments()[x];
                    if(argument.startsWith("<")) {
                        message.appendSibling(new TextComponentString(argument + " ").setStyle(new Style().setColor(TextFormatting.RED)));
                    } else {
                        message.appendSibling(new TextComponentString(argument + " ").setStyle(new Style().setColor(TextFormatting.GRAY)));
                    }
                }
            }
            message.appendSibling(new TextComponentString("- ").setStyle(new Style().setColor(TextFormatting.GRAY)));
            message.appendSibling(new TextComponentString(f.getPurpose()).setStyle(new Style().setColor(TextFormatting.BLUE)));

            sender.sendMessage(message);
        }
    }

}
