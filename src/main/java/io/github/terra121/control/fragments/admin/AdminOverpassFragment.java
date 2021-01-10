package io.github.terra121.control.fragments.admin;

import io.github.terra121.TerraConfig;
import io.github.terra121.TerraConstants;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.dataset.OpenStreetMaps;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AdminOverpassFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.commands.overpass.usage"))));
            return;
        }
        String inUse = OpenStreetMaps.getOverpassEndpoint();
        boolean isDefault = inUse.equals(TerraConfig.serverOverpassDefault);
        boolean hasFallback = !TerraConfig.serverOverpassFallback.isEmpty();
        switch (args[0]) {
            case "status":
                String t = isDefault ? "terra121.commands.overpass.status.default" : "terra121.commands.overpass.status.fallback";
                sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.BLUE + TranslateUtil.format(t, inUse))));
                break;
            case "list":
                sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.BLUE + TranslateUtil.format("terra121.commands.overpass.list.default", TerraConfig.serverOverpassDefault))));
                sender.sendMessage(new TextComponentString(TextFormatting.BLUE + (hasFallback ?
                        TranslateUtil.format("terra121.commands.overpass.list.fallback", TerraConfig.serverOverpassFallback) :
                        TranslateUtil.format("terra121.commands.overpass.list.nofallback"))));

                break;
            case "default":
                OpenStreetMaps.setOverpassEndpoint(TerraConfig.serverOverpassDefault);
                OpenStreetMaps.cancelFallbackThread();
                sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.GREEN + TranslateUtil.translate("terra121.commands.overpass.set.default"))));
                break;
            case "fallback":
                if (!hasFallback) {
                    sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.commands.overpass.list.nofallback"))));
                    return;
                }

                OpenStreetMaps.setOverpassEndpoint(TerraConfig.serverOverpassFallback);
                OpenStreetMaps.cancelFallbackThread();
                sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.GREEN + TranslateUtil.translate("terra121.commands.overpass.set.fallback"))));

                break;
            default:
                sender.sendMessage(TerraConstants.TextConstants.title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.commands.overpass.usage"))));
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
