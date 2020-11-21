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
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TerraWhereFragment extends CommandFragment {
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

        if (!(gen instanceof EarthTerrainProcessor)) {
            sender.sendMessage(TerraConstants.TextConstants.getNotTerra());
            return;
        }

        Vec3d pos = sender.getPositionVector();
        Entity e = sender.getCommandSenderEntity();
        String senderName = sender.getName();
        if (args.length > 0) {
            if(hasAdminPermission(sender)) e = sender.getEntityWorld().getPlayerEntityByName(args[0]);
            if (e == null) {
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.unknownplayer"), TextFormatting.RED)));
                return;
            }

            pos = e.getPositionVector();
            senderName = e.getName();
        }

        EarthTerrainProcessor terrain = (EarthTerrainProcessor) gen;
        GeographicProjection projection = terrain.projection;



        double[] result = projection.toGeo(pos.x, pos.z);
        sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Location of ", TextFormatting.GRAY), new TextElement(senderName, TextFormatting.BLUE)));
        if(Double.isNaN(result[0])) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.fragment.terra.where.notproj"), TextFormatting.RED)));
            return;
        }

        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Location: ", TextFormatting.GRAY), new TextElement("" + result[1], TextFormatting.BLUE),
                new TextElement(", ", TextFormatting.GRAY), new TextElement("" + result[0], TextFormatting.BLUE)));
    }

    @Override
    public String[] getName() {
        return new String[]{"where", "ou"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.where.purpose");
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
