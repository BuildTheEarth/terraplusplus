package net.buildtheearth.terraplusplus.control.fragments;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.buildtheearth.terraplusplus.control.Command;
import net.buildtheearth.terraplusplus.util.ChatUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class FragmentManager extends Command {

    public FragmentManager(String command) {
        this.commandBase = String.format("/%s ", command);
    }

    private final Map<String, CommandFragment> fragments = Maps.newHashMap();
    private final List<CommandFragment> singleFragments = Lists.newArrayList();
    private final String commandBase;

    protected void register(CommandFragment c) {
        singleFragments.add(c);
        for(String name : c.getName()) {
            fragments.put(name, c);
        }
    }

    protected void executeFragment(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 0) {
            CommandFragment fragment = fragments.get(args[0].toLowerCase(Locale.ROOT));
            if(fragment != null) {
                fragment.execute(server, sender, selectArray(args));
                return;
            }
        }
        displayCommands(sender);
    }

    private void displayCommands(ICommandSender sender) {
        for(int i = 0; i < 2; i++) {
            sender.sendMessage(ChatUtil.combine(""));
        }

        sender.sendMessage(ChatUtil.combine(TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH, "================",
                TextFormatting.DARK_GREEN + "" + TextFormatting.BOLD, " Terra++ ", TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH, "================"));
        sender.sendMessage(ChatUtil.combine(""));

        for(CommandFragment f : singleFragments) {
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
        sender.sendMessage(ChatUtil.combine(""));
        sender.sendMessage(ChatUtil.combine(TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH, "=========================================="));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        List<String> tabCompletions = new ArrayList<>();
        for(String s : fragments.keySet()) {
            if(args.length == 0) {
                tabCompletions.add(s);
            } else if(s.startsWith(args[0].toLowerCase())) {
                tabCompletions.add(s);
            }
        }
        return tabCompletions;
    }

    private String[] selectArray(String[] args) {
        List<String> array = new ArrayList<>();
        for(int i = 1; i < args.length; i++) {
            array.add(args[i]);
        }
        return array.toArray(array.toArray(new String[array.size()]));
    }
}
