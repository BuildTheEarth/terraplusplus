package net.buildtheearth.terraplusplus.control;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.geo.CoordinateParseUtils;
import net.buildtheearth.terraplusplus.util.geo.EllipsoidalCoordinates;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TerraTeleport extends Command {

    @Override
    public String getName() {
        return "tpll";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TerraConstants.defaultCommandNode + "tpll.usage";
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
        World world = server.getEntityWorld();
        IChunkProvider cp = world.getChunkProvider();

        if (!(cp instanceof CubeProviderServer)) {
            throw new CommandException(TerraConstants.MODID + ".error.notcc");
        }

        ICubeGenerator gen = ((CubeProviderServer) cp).getCubeGenerator();

        if (!(gen instanceof EarthGenerator)) {
            throw new CommandException(TerraConstants.MODID + ".error.notterra");
        }

        EarthGenerator terrain = (EarthGenerator) gen;

        if (args.length == 0) {
            this.usage(sender);
            return;
        }

        List<EntityPlayerMP> receivers = new ArrayList<>();

        if (this.hasPermission(sender, TerraConstants.othersCommandNode)) {
            try {
                receivers = getPlayers(server, sender, args[0]);
            } catch (CommandException ignored) {
            }
        }

        if (!receivers.isEmpty() && args.length < 2) {
            this.usage(sender);
            return;
        }

        double altitude = Double.NaN;
        EllipsoidalCoordinates defaultCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(args).trim());

        if (defaultCoords == null) {
            EllipsoidalCoordinates possiblePlayerCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.selectArray(args, 1)));
            if (possiblePlayerCoords != null) {
                defaultCoords = possiblePlayerCoords;
            }
        }

        EllipsoidalCoordinates possibleHeightCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.inverseSelectArray(args, args.length - 1)));
        if (possibleHeightCoords != null) {
            defaultCoords = possibleHeightCoords;
            try {
                altitude = Double.parseDouble(args[args.length - 1]);
            } catch (Exception e) {
                altitude = Double.NaN;
            }
        }

        EllipsoidalCoordinates possibleHeightNameCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.inverseSelectArray(this.selectArray(args, 1), this.selectArray(args, 1).length - 1)));
        if (possibleHeightNameCoords != null) {
            defaultCoords = possibleHeightNameCoords;
            try {
                altitude = Double.parseDouble(this.selectArray(args, 1)[this.selectArray(args, 1).length - 1]);
            } catch (Exception e) {
                altitude = Double.NaN;
            }
        }

        if (defaultCoords == null) {
            this.usage(sender);
            return;
        }

        double[] proj;

        try {
            proj = terrain.projection.fromGeo(defaultCoords.longitudeDegrees(), defaultCoords.latitudeDegrees());
        } catch (Exception e) {
            sender.sendMessage(TerraUtils.combine(TextFormatting.RED, TerraUtils.translate(TerraConstants.MODID + ".error.numbers")));
            return;
        }

        CompletableFuture<Double> altFuture;
        if (Double.isNaN(altitude)) {
            try {
                altFuture = terrain.datasets
                        .<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                        .getAsync(defaultCoords.longitudeDegrees(), defaultCoords.latitudeDegrees())
                        .thenApply(a -> a + 1.0d);
            } catch (OutOfProjectionBoundsException e) { //out of bounds, notify user
                sender.sendMessage(TerraUtils.titleAndCombine(TextFormatting.RED, TerraUtils.translate(TerraConstants.MODID + ".error.numbers")));
                return;
            }
        } else {
            altFuture = CompletableFuture.completedFuture(altitude);
        }

        if (receivers.isEmpty() && sender instanceof EntityPlayerMP) {
            receivers.add((EntityPlayerMP) sender);
        }
        List<EntityPlayerMP> finalReceivers = receivers;
        EllipsoidalCoordinates finalDefaultCoords = defaultCoords;
        altFuture.thenAccept(s -> FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            for (EntityPlayerMP p : finalReceivers) {
                if (p.getName().equalsIgnoreCase(sender.getName())) {
                    p.sendMessage(TerraUtils.titleAndCombine(TextFormatting.GRAY, "Teleporting to ", TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.latitudeDegrees()),
                            TextFormatting.GRAY, ", ", TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.longitudeDegrees())));
                } else if (!sender.getName().equalsIgnoreCase("@")) {
                    p.sendMessage(TerraUtils.titleAndCombine(TextFormatting.GRAY, "Summoned to ", TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.latitudeDegrees()),
                            TextFormatting.GRAY, ", ", TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.longitudeDegrees()), TextFormatting.GRAY, " by ", TextFormatting.RED, sender.getDisplayName()));
                } else {
                    p.sendMessage(TerraUtils.titleAndCombine(TextFormatting.GRAY, "Summoned to ", TextFormatting.BLUE, TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.latitudeDegrees()),
                            TextFormatting.GRAY, ", ", TextFormatting.BLUE, this.formatDecimal(finalDefaultCoords.longitudeDegrees()), TextFormatting.GRAY));
                }
                p.setPositionAndUpdate(proj[0], s, proj[1]);
            }
        }));
    }

    private String formatDecimal(double val) {
        return new DecimalFormat("##.#####").format(val);
    }

    /**
     * Gets all objects in a string array above a given index
     *
     * @param args  Initial array
     * @param index Starting index
     * @return Selected array
     */
    private String[] selectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = index; i < args.length; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));
    }

    private String[] inverseSelectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));

    }

    /**
     * Gets a space seperated string from an array
     *
     * @param args A string array
     * @return The space seperated String
     */
    private String getRawArguments(String[] args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1) {
            return args[0];
        }

        StringBuilder arguments = new StringBuilder(args[0].replace((char) 176, (char) 32).trim());

        for (int x = 1; x < args.length; x++) {
            arguments.append(" ").append(args[x].replace((char) 176, (char) 32).trim());
        }

        return arguments.toString();
    }

    private void usage(ICommandSender sender) {
        if (this.hasPermission(sender, TerraConstants.othersCommandNode)) {
            sender.sendMessage(TerraUtils.combine(TextFormatting.RED, TerraUtils.translate(TerraConstants.defaultCommandNode + "tpll.others.usage")));
        } else {
            sender.sendMessage(TerraUtils.combine(TextFormatting.RED, TerraUtils.translate(TerraConstants.defaultCommandNode + "tpll.usage")));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
    }
}
