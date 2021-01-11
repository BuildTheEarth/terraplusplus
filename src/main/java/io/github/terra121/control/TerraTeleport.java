package io.github.terra121.control;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.TerraConstants;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.ChatUtil;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

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
            sender.sendMessage(ChatUtil.getNoPermission());
            return;
        }

        if (this.isOp(sender) || !(sender instanceof EntityPlayer)) {
            if(!hasPermission(TerraConstants.controlCommandNode + "tpll", sender)) {
                sender.sendMessage(ChatUtil.getNoPermission());
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
                    sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.commands.tpll.usage")));
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
                    sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.commands.tpll.usage")));
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
                    sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.error.numbers")));
                    return;
                }

                CompletableFuture<String> altFuture;
                if (alt == null) {
                    try {
                        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Computing destination altitude..."));
                        altFuture = terrain.datasets.heights.getAsync(lon, lat)
                                .thenApply(a -> String.valueOf(a + 1.0d));
                    } catch (OutOfProjectionBoundsException e) { //out of bounds, notify user
                        sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.numbers")));
                        return;
                    }
                } else {
                    altFuture = CompletableFuture.completedFuture(alt);
                }

                ICommandSender _sender = sender;
                altFuture.thenAccept(s -> FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                    _sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Teleported to ", TextFormatting.BLUE, new DecimalFormat("##.#####").format(lat),
                                                                           TextFormatting.GRAY, ", ", TextFormatting.BLUE, new DecimalFormat("##.#####").format(lon)));

                    FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(
                            FMLCommonHandler.instance().getMinecraftServerInstance(), String.format("tp %s %s %s %s", _sender.getName(), proj[0], s, proj[1]));
                }));
            }
        }
    }

    private boolean isOp(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return PermissionAPI.hasPermission((EntityPlayer) sender, "terra121.commands.tpll");
        }
        return sender.canUseCommand(2, "");
    }


}
