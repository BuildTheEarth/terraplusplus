package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.generator.EarthGeneratorSettings;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraWorldFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.fragment.terra.world.header"), TextFormatting.GRAY)));

        World world = sender.getEntityWorld();
        IChunkProvider cp = world.getChunkProvider();

        if (!(cp instanceof CubeProviderServer)) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("World Type: ", TextFormatting.BLUE), new TextElement("Vanilla", TextFormatting.RED)));
            return;
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthGenerator)) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("World Type: ", TextFormatting.BLUE), new TextElement("Not Earth World", TextFormatting.RED)));
            return;
        }

        EarthGenerator terrain = (EarthGenerator) gen;
        EarthGeneratorSettings.JsonSettings projectionSettings = terrain.cfg.settings;

        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("World Type: ", TextFormatting.BLUE), new TextElement("Earth World", TextFormatting.GREEN)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Projection: ", TextFormatting.BLUE), new TextElement(projectionSettings.projection, TextFormatting.GREEN),
                new TextElement(" ["+ projectionSettings.orentation.name() + "]", TextFormatting.GRAY)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Scale: ", TextFormatting.BLUE), new TextElement("[" + projectionSettings.scaleX + ", " + projectionSettings.scaleY + "]", TextFormatting.GRAY)));

        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("", TextFormatting.RESET)));

        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Roads: ", TextFormatting.BLUE), projectionSettings.roads ? new TextElement("True", TextFormatting.GREEN) :
                new TextElement("False", TextFormatting.RED)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Buildings: ", TextFormatting.BLUE), projectionSettings.buildings ? new TextElement("True", TextFormatting.GREEN) :
                new TextElement("False", TextFormatting.RED)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("OSM Water: ", TextFormatting.BLUE), projectionSettings.osmwater ? new TextElement("True", TextFormatting.GREEN) :
                new TextElement("False", TextFormatting.RED)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Smooth Blend: ", TextFormatting.BLUE), projectionSettings.smoothblend ? new TextElement("True", TextFormatting.GREEN) :
                new TextElement("False", TextFormatting.RED)));
    }

    @Override
    public String[] getName() {
        return new String[]{"world"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.world.purpose");
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
