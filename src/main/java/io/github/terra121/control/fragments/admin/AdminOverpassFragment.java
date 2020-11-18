package io.github.terra121.control.fragments.admin;

import io.github.terra121.TerraConfig;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.dataset.OpenStreetMaps;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AdminOverpassFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.overpass.usage"), TextFormatting.RED)));
            return;
        }
        String inUse = OpenStreetMaps.getOverpassEndpoint();
        boolean isDefault = inUse.equals(TerraConfig.serverOverpassDefault);
        boolean hasFallback = !TerraConfig.serverOverpassFallback.isEmpty();
        switch (args[0]) {
            case "status":
                String t = isDefault ? "terra121.commands.overpass.status.default" : "terra121.commands.overpass.status.fallback";
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.format(t, inUse), TextFormatting.BLUE)));
                break;
            case "list":
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.format("terra121.commands.overpass.list.default", TerraConfig.serverOverpassDefault), TextFormatting.BLUE)));
                sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(hasFallback ?
                        TranslateUtil.format("terra121.commands.overpass.list.fallback", TerraConfig.serverOverpassFallback) :
                        TranslateUtil.format("terra121.commands.overpass.list.nofallback"), TextFormatting.BLUE)));
                break;
            case "default":
                OpenStreetMaps.setOverpassEndpoint(TerraConfig.serverOverpassDefault);
                OpenStreetMaps.cancelFallbackThread();
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.overpass.set.default"), TextFormatting.GREEN)));
                break;
            case "fallback":
                if (!hasFallback) {
                    sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.overpass.list.nofallback"), TextFormatting.RED)));
                    return;
                }

                OpenStreetMaps.setOverpassEndpoint(TerraConfig.serverOverpassFallback);
                OpenStreetMaps.cancelFallbackThread();
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.overpass.set.fallback"), TextFormatting.GREEN)));

                break;
            default:
                sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.overpass.usage"), TextFormatting.RED)));
        }

    }

    @Override
    public String[] getName() {
        return new String[]{"overpass", "o"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.admin.overpass.purpose");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"<status|list|default|fallback>"};
    }

    @Override
    public String getPermission() {
        return "terra121.admin";
    }
}
