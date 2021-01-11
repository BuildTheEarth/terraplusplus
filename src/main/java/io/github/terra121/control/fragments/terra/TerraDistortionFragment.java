package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.EarthTerrainProcessor;
import io.github.terra121.TerraConstants;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.ChatUtil;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraDistortionFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        IChunkProvider cp = world.getChunkProvider();

        if (!(cp instanceof CubeProviderServer)) {
            sender.sendMessage(ChatUtil.getNotCC());
            return;
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthTerrainProcessor)) {
            sender.sendMessage(ChatUtil.getNotTerra());
            return;
        }

        EarthTerrainProcessor terrain = (EarthTerrainProcessor) gen;
        GeographicProjection projection = terrain.projection;

        double[] c = projection.toGeo(sender.getPositionVector().x, sender.getPositionVector().z);
        c = projection.tissot(c[0], c[1], 0.0000001);

        if(c == null || Double.isNaN(c[0])) {
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.fragment.terra.where.notproj")));
            return;
        }
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Distortion:"));
        sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.format("terra121.commands.terra.tissot", Math.sqrt(Math.abs(c[0])), c[1] * 180.0 / Math.PI)));
    }

    @Override
    public String[] getName() {
        return new String[]{"distortion", "tissot", "tiss"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.distortion.purpose").getUnformattedComponentText();
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
