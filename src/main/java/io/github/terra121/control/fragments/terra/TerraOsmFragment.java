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
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraOsmFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(sender instanceof MinecraftServer && args.length < 1) {
            sender.sendMessage(ChatUtil.getPlayerOnly());
            return;
        }

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

        Vec3d pos = sender.getPositionVector();
        Entity e = sender.getCommandSenderEntity();
        String senderName = sender.getName();
        if (args.length > 0) {
            if(hasAdminPermission(sender)) e = sender.getEntityWorld().getPlayerEntityByName(args[0]);
            if (e == null) {
                sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.unknownplayer")));
                return;
            }

            pos = e.getPositionVector();
            senderName = e.getName();
        }

        EarthTerrainProcessor terrain = (EarthTerrainProcessor) gen;
        GeographicProjection projection = terrain.projection;

        double[] result = projection.toGeo(pos.x, pos.z);
        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY , "Location of ", TextFormatting.BLUE, senderName, TextFormatting.GRAY, " on maps:"));
        sender.sendMessage(ChatUtil.combine(TextFormatting.GRAY, "Google Maps: ", new TextComponentString("Click Here").setStyle(
                new Style().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/search/?api=1&query=" + result[1] + "," + result[0])))));
        sender.sendMessage(ChatUtil.combine(TextFormatting.GRAY + "OSM: ", new TextComponentString("Click Here").setStyle(
                new Style().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, String.format("https://www.openstreetmap.org/#map=17/%.5f/%.5f", result[1], result[0]))))));

    }

    @Override
    public String[] getName() {
        return new String[]{"osm", "map"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.osm.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return new String[]{"[player]"};
    }

    @Override
    public String getPermission() {
        return "terra121.commands.terra";
    }
}
