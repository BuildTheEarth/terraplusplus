package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.EarthTerrainProcessor;
import io.github.terra121.TerraConstants;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.dataset.OpenStreetMaps;
import io.github.terra121.dataset.Water;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.ChatUtil;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraInvertWaterFragment extends CommandFragment {
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

        if (!terrain.cfg.settings.osmwater || terrain.osm == null || terrain.osm.water == null) {
            sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.error.nowtr")));
            return;
        }

        double[] c = projection.toGeo(sender.getPositionVector().x, sender.getPositionVector().z);

        if(c == null || Double.isNaN(c[0])) {
            sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.fragment.terra.where.notproj")));
            return;
        }

        OpenStreetMaps.Coord region = terrain.osm.getRegion(c[0], c[1]);

        Water water = terrain.osm.water;
        water.doingInverts = true;

        boolean restore = false;
        if(args.length > 0)
            if(!(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /terra invertwater [restore:<true/false>]"));
                return;
            } else {
                if(args[0].equalsIgnoreCase("true")) restore = true;
            }

        if (restore ? water.inverts.remove(region) : water.inverts.add(region)) {
            sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.format(restore ? "terra121.commands.terra.rstwtr" : "terra121.commands.terra.invwtr", region.x, region.y)));
            return;
        }

        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.format("terra121.error.invwtr", region.x, region.y)));
    }

    @Override
    public String[] getName() {
        return new String[]{"invertwater", "invwtr", "restorewater", "rstwtr"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.winvert.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return new String[]{"[restore:<true/false>]"};
    }

    @Override
    public String getPermission() {
        return "terra121.commands.terra.utility";
    }
}
