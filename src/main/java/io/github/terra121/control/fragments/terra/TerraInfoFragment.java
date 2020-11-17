package io.github.terra121.control.fragments.terra;

import io.github.terra121.TerraConstants;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class TerraInfoFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Terra++ v." + TerraConstants.version, TextFormatting.RED),
                new TextElement(" by ", TextFormatting.GRAY), new TextElement("bitbyte2015, noahhusby, DaPorkchop_, Rude Yeti, Barteks2x, and the BTE Dev Team", TextFormatting.BLUE)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Original mod by orangeadam3", TextFormatting.GOLD)));
    }

    @Override
    public String[] getName() {
        return new String[]{"info"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.info.purpose");
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
