package net.buildtheearth.terraplusplus.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.control.fragments.CommandFragment;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.ChatUtil;
import net.buildtheearth.terraplusplus.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
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

        if (!(gen instanceof EarthGenerator)) {
            sender.sendMessage(ChatUtil.getNotTerra());
            return;
        }

        EarthGenerator terrain = (EarthGenerator) gen;
        GeographicProjection projection = terrain.projection;

        double[] c = new double[0];
        try {
            c = projection.toGeo(sender.getPositionVector().x, sender.getPositionVector().z);
            c = projection.tissot(c[0], c[1], 0.0000001);
        } catch (OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }

        if (c == null || Double.isNaN(c[0])) {
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MODID + ".fragment.terra.where.notproj")));
            return;
        }
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Distortion:"));
        sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.format(TerraConstants.MODID + ".command.terra.tissot", Math.sqrt(Math.abs(c[0])), c[1] * 180.0 / Math.PI)));
    }

    @Override
    public String[] getName() {
        return new String[]{ "distortion", "tissot", "tiss" };
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate(TerraConstants.MODID + ".fragment.terra.distortion.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public String getPermission() {
        return TerraConstants.MODID + ".commands.terra";
    }
}
