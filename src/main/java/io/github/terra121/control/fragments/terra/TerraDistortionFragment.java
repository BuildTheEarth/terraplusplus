package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.EarthTerrainProcessor;
import io.github.terra121.TerraConstants;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.TranslateUtil;
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
            sender.sendMessage(TerraConstants.TextConstants.notCC);
            return;
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthTerrainProcessor)) {
            sender.sendMessage(TerraConstants.TextConstants.notTerra);
            return;
        }

        EarthTerrainProcessor terrain = (EarthTerrainProcessor) gen;
        GeographicProjection projection = terrain.projection;

        double[] c = projection.toGeo(sender.getPositionVector().x, sender.getPositionVector().z);
        c = projection.tissot(c[0], c[1], 0.0000001);

        if(c == null || Double.isNaN(c[0])) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.fragment.terra.where.notproj"), TextFormatting.RED)));
            return;
        }

        sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Distortion: ", TextFormatting.GRAY)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.format("terra121.commands.terra.tissot", Math.sqrt(Math.abs(c[0])), c[1] * 180.0 / Math.PI), TextFormatting.RED)));
    }

    @Override
    public String[] getName() {
        return new String[]{"distortion", "tissot", "tiss"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.distortion.purpose");
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
