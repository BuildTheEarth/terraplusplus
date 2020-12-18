package io.github.terra121.control;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.TerraConstants;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import java.text.DecimalFormat;

public class TerraTeleport extends Command {

    @Override
    public String getName() {
        return "tpll";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "terra121.commands.tpll.usage";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!hasPermission(TerraConstants.controlCommandNode + "tpll", sender)) {
            sender.sendMessage(TerraConstants.TextConstants.getNoPermission());
            return;
        }

        if (this.isOp(sender) || !(sender instanceof EntityPlayer)) {
            World world = server.getEntityWorld();
            IChunkProvider cp = world.getChunkProvider();

            if (!(cp instanceof CubeProviderServer)) {
                throw new CommandException("terra121.error.notcc");
            }

            ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

            if (!(gen instanceof EarthGenerator)) {
                throw new CommandException("terra121.error.notterra");
            }

            EarthGenerator terrain = (EarthGenerator) gen;

            if (args.length == 0) {
                sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.tpll.usage"), TextFormatting.RED)));
                return;
            }

            String[] splitCoords = args[0].split(",");
            String alt = null;
            if (splitCoords.length == 2 && args.length < 4) { // lat and long in single arg
                if (args.length > 1) {
                    alt = args[1];
                }
                if (args.length > 2) {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[2]);
                    if (player != null) {
                        sender = player;
                    }
                }
                args = splitCoords;
            } else if (args.length == 3) {
                EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[2]);
                if (player != null) {
                    sender = player;
                } else {
                    alt = args[2];
                }
            } else if (args.length == 4) {
                EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[3]);
                if (player != null) {
                    sender = player;
                }
                alt = args[2];
            }
            if (args[0].endsWith(",")) {
                args[0] = args[0].substring(0, args[0].length() - 1);
            }
            if (args.length > 1 && args[1].endsWith(",")) {
                args[1] = args[1].substring(0, args[1].length() - 1);
            }
            if (args.length != 2 && args.length != 3 && args.length != 4) {
                sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.commands.tpll.usage"), TextFormatting.RED)));
                return;
            }

            double lon;
            double lat;
            double[] proj;

            try {
                lat = Double.parseDouble(args[0]);
                lon = Double.parseDouble(args[1]);
                if (alt != null) {
                    alt = Double.toString(Double.parseDouble(alt));
                }
                proj = terrain.projection.fromGeo(lon, lat);
            } catch (Exception e) {
                sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(TranslateUtil.translate("terra121.error.numbers"), TextFormatting.RED)));
                return;
            }

            if (alt == null) {
                alt = String.valueOf(terrain.heights.get(lon, lat) + 1);
            }

            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Teleported to ", TextFormatting.GRAY), new TextElement(new DecimalFormat("##.#####").format(lat), TextFormatting.BLUE),
                    new TextElement(", ", TextFormatting.GRAY), new TextElement(new DecimalFormat("##.#####").format(lon), TextFormatting.BLUE)));

            FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(
                    FMLCommonHandler.instance().getMinecraftServerInstance(), String.format("tp %s %s %s %s", sender.getName(), proj[0], alt, proj[1]));
        }
    }

    private boolean isOp(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, "terra121.commands.tpll");
        }
        return sender.canUseCommand(2, "");
    }


}
