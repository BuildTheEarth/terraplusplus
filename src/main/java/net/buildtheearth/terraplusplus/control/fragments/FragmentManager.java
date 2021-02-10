package net.buildtheearth.terraplusplus.control.fragments;

import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.control.Command;
import net.buildtheearth.terraplusplus.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public abstract class FragmentManager extends Command {

    public FragmentManager(String title, String command) {
        this.title = String.format(" %s", title);
        this.commandBase = String.format("/%s ", command);
        register(new CommandFragment() {
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
                return TranslateUtil.translate(TerraConstants.MOD_ID + ".fragment.help.purpose").getUnformattedComponentText();
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

    private List<ICommandFragment> fragments = new ArrayList<>();
    private final String title;
    private final String commandBase;

    protected void register(ICommandFragment c) {
        fragments.add(c);
    }

    protected void executeFragment(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 0) {
            for (ICommandFragment f : fragments) {
                for(String c : f.getName()) {
                    if (c.equalsIgnoreCase(args[0])) {
                        f.execute(server, sender, selectArray(args, 1));
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
            if(page > Math.ceil(fragments.size() / 7.0)) page = 1;
        }

        sender.sendMessage(new TextComponentString(title + ":").setStyle(new Style().setColor(TextFormatting.GRAY)));
        for(int xf = (page - 1) * 7; xf < Math.min(((page - 1) * 7) + 7, fragments.size()); xf++) {
            ICommandFragment f = fragments.get(xf);

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

        if(Math.ceil(fragments.size() / 7.0) < 2) return;

        String end = page >= Math.ceil(fragments.size() / 7.0) ? "Use '" + commandBase + "help " + (page - 1) + "' to see the previous page."
                : "Use '" + commandBase + "help " + (page + 1) + "' to see the next page.";
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + end));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        List<String> tabCompletions = new ArrayList<>();

        for(ICommandFragment fragment : fragments)
            for(String s : fragment.getName())
                if(args.length == 0)
                    tabCompletions.add(s);
                else if(s.startsWith(args[0].toLowerCase()))
                    tabCompletions.add(s);

        return tabCompletions;
    }

    private String[] selectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for(int i = index; i < args.length; i++)
            array.add(args[i]);

        return array.toArray(array.toArray(new String[array.size()]));
    }
}
