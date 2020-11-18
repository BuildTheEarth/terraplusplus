package io.github.terra121.control.fragments;

import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.Command;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import scala.Int;

import java.util.ArrayList;
import java.util.List;

public abstract class FragmentManager extends Command {

    public FragmentManager() {
        registerCommandFragment(new CommandFragment() {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                displayCommands(sender, args);
            }

            @Override
            public String[] getName() {
                return new String[]{"help"};
            }

            @Override
            public String getPurpose() {
                return TranslateUtil.translate("terra121.fragment.help.purpose");
            }

            @Override
            public String[] getArguments() {
                return new String[]{"[page]"};
            }

            @Override
            public String getPermission() {
                return "";
            }
        });
    }

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
        int page = 1;
        if(args != null) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e) { }
            if(page > Math.ceil(commandFragments.size() / 7.0)) page = 1;
        }

        sender.sendMessage(new TextComponentString(title + ":").setStyle(new Style().setColor(TextFormatting.GRAY)));
        for(int xf = (page - 1) * 7; xf < Math.min(((page - 1) * 7) + 7, commandFragments.size()); xf++) {
            ICommandFragment f = commandFragments.get(xf);

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

        if(Math.ceil(commandFragments.size() / 7.0) < 2) return;

        String end = page >= Math.ceil(commandFragments.size() / 7.0) ? "Use '" + commandBase + "help " + (page - 1) + "' to see the previous page."
                : "Use '" + commandBase + "help " + (page + 1) + "' to see the next page.";
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(end, TextFormatting.GOLD)));
    }

}
