package net.buildtheearth.terraplusplus.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.control.fragments.CommandFragment;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.util.ChatUtil;
import net.buildtheearth.terraplusplus.util.TranslateUtil;
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
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MOD_ID + ".fragment.terra.world.header")));

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

        EarthGeneratorSettings settings = ((EarthGenerator) gen).settings;

        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "World Type: ", TextFormatting.GREEN, "Earth World"));
        sender.sendMessage(ChatUtil.combine(TextFormatting.RESET));

        sender.sendMessage(ChatUtil.combine(TextFormatting.BLUE, "Projection: ", TextFormatting.GREEN, settings.projection().toString()));
        sender.sendMessage(this.boolComponent("Default heights", settings.useDefaultHeights()));
        sender.sendMessage(this.boolComponent("Default trees", settings.useDefaultTreeCover()));
    }

    @Override
    public String[] getName() {
        return new String[]{ "world" };
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate(TerraConstants.MOD_ID + ".fragment.terra.world.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public String getPermission() {
        return TerraConstants.MOD_ID + ".commands.terra";
    }

    private ITextComponent boolComponent(String name, boolean value) {
        return ChatUtil.combine(TextFormatting.BLUE, name, ": ", (value ? TextFormatting.GREEN + "Yes" : TextFormatting.RED + "No"));
    }
}
