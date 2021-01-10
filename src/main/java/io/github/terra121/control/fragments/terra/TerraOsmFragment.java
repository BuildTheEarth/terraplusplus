package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.TerraConstants;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
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
            sender.sendMessage(TerraConstants.TextConstants.getPlayerOnly());
            return;
        }

        World world = sender.getEntityWorld();
        IChunkProvider cp = world.getChunkProvider();

        if (!(cp instanceof CubeProviderServer)) {
            sender.sendMessage(TerraConstants.TextConstants.getNotCC());
            return;
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthGenerator)) {
            sender.sendMessage(TerraConstants.TextConstants.getNotTerra());
            return;
        }

        Vec3d pos = sender.getPositionVector();
        Entity e = sender.getCommandSenderEntity();
        String senderName = sender.getName();
        if (args.length > 0) {
            if(hasAdminPermission(sender)) e = sender.getEntityWorld().getPlayerEntityByName(args[0]);
            if (e == null) {
                sender.sendMessage(TerraConstants.TextConstants.title(TextFormatting.RED + TranslateUtil.translate("terra121.error.unknownplayer")));
                return;
            }

            pos = e.getPositionVector();
            senderName = e.getName();
        }

        EarthGenerator terrain = (EarthGenerator) gen;
        GeographicProjection projection = terrain.projection;

        try {
            double[] result = projection.toGeo(pos.x, pos.z);
            sender.sendMessage(TerraConstants.TextConstants.title(TextFormatting.GRAY + "Location of " + TextFormatting.BLUE + senderName + TextFormatting.GRAY + " on maps:"));
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Google Maps: ").appendSibling(new TextComponentString("Click Here").setStyle(
                    new Style().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/search/?api=1&query=" + result[1] + "," + result[0])))));
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "OSM: ").appendSibling(new TextComponentString("Click Here").setStyle(
                    new Style().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, String.format("https://www.openstreetmap.org/#map=17/%.5f/%.5f", result[1], result[0]))))));
        } catch (OutOfProjectionBoundsException e1) { //out of bounds, print error
            sender.sendMessage(TerraConstants.TextConstants.title(TextFormatting.RED + "Unknown position!"));
        }
    }

    @Override
    public String[] getName() {
        return new String[]{"osm", "map"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.osm.purpose");
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
