package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.EarthGeneratorSettings;
import io.github.terra121.util.ChatUtil;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraWorldFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.fragment.terra.world.header")));

        World world = sender.getEntityWorld();
        IChunkProvider cp = world.getChunkProvider();

        if (!(cp instanceof CubeProviderServer)) {
            sender.sendMessage(new TextComponentString(TextFormatting.BLUE + "World Type: " + TextFormatting.RED + "Vanilla"));
            return;
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthGenerator)) {
            sender.sendMessage(new TextComponentString(TextFormatting.BLUE + "World Type: " + TextFormatting.RED + "Not Earth World"));
            return;
        }

        EarthGenerator terrain = (EarthGenerator) gen;
        EarthGeneratorSettings.JsonSettings projectionSettings = terrain.cfg.settings;

        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "World Type: ", TextFormatting.GREEN, "Earth World"));
        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "Projection: ", TextFormatting.GREEN, projectionSettings.projection,
                                                   TextFormatting.GRAY, String.format(" [%s]", projectionSettings.orentation.name())));
        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "Scale: ", TextFormatting.GRAY,
                                                   String.format("[%s, %s]", projectionSettings.scaleX, projectionSettings.scaleY)));
        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "Offset: ", TextFormatting.GRAY,
                                                   String.format("[%s, %s]", projectionSettings.offsetX, projectionSettings.offsetY)));
        sender.sendMessage(ChatUtil.combine(TextFormatting.RESET));

        sender.sendMessage(boolComponent("Roads", projectionSettings.roads));
        sender.sendMessage(boolComponent("Buildings", projectionSettings.buildings));
        sender.sendMessage(boolComponent("OSM Water", projectionSettings.osmwater));
        sender.sendMessage(boolComponent("Smooth Blending", projectionSettings.smoothblend));
    }

    @Override
    public String[] getName() {
        return new String[]{"world"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.world.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public String getPermission() {
        return "terra121.commands.terra";
    }

    private ITextComponent boolComponent(String name, boolean value) {
        return ChatUtil.combine(TextFormatting.BLUE, name, ": ", (value ? TextFormatting.GREEN + "True" : TextFormatting.RED + "False"));
    }
}
