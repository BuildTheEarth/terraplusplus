package net.buildtheearth.terraplusplus.control.fragments.terra;

import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.control.fragments.CommandFragment;
import net.buildtheearth.terraplusplus.util.ChatUtil;
import net.buildtheearth.terraplusplus.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TerraInfoFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, "Terra++ v", TerraConstants.version,
                TextFormatting.GRAY, " by the ", TextFormatting.BLUE, "BTE Development Community"));
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Original mod by orangeadam3 and shejan0"));
    }

    @Override
    public String[] getName() {
        return new String[]{"info"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.info.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public String getPermission() {
        return "terra121.commands.terra";
    }
}
