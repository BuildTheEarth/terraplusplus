package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.EarthBiomeProvider;
import io.github.terra121.TerraConstants;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;


public class TerraEnvironmentFragment extends CommandFragment {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        BiomeProvider bp = sender.getEntityWorld().getBiomeProvider();
        if (!(bp instanceof EarthBiomeProvider)) { //must have normal biome provider
            sender.sendMessage(TerraConstants.TextConstants.getNotTerra());
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

        EarthGenerator terrain = (EarthGenerator) gen;
        GeographicProjection projection = terrain.projection;

        double[] c = this.getCoordArgs(sender, args, projection);

        c = ((EarthBiomeProvider) bp).getEnv(c[0], c[1]);
        sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Environment Data: ", TextFormatting.GRAY)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(String.format("%.1f \\U+00B0C", c[1]), TextFormatting.RED)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(String.format("%.1f mm/yr", c[2]), TextFormatting.RED)));
        sender.sendMessage(ChatHelper.makeTextComponent(new TextElement(String.format("Soil Id: %d", (int) c[0]), TextFormatting.RED)));
    }

    @Override
    public String[] getName() {
        return new String[]{ "environment", "env" };
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.environment.purpose");
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public String getPermission() {
        return "terra121.commands.terra";
    }

    private double[] getCoordArgs(ICommandSender sender, String[] args, GeographicProjection projection) {
        if (args.length == 3) {
            return this.getNumbers(args[2], args[1]);
        } else if (args.length == 2) {
            double[] c = this.getPlayerCoords(sender, args[1], projection);
            return c;
        } else {
            double[] c = this.getPlayerCoords(sender, null, projection);
            return c;
        }
    }

    private double[] getNumbers(String s1, String s2) {
        double x = 0;
        double y = 0;
        try {
            x = Double.parseDouble(s1);
            y = Double.parseDouble(s2);
        } catch (Exception e) {
        }

        return new double[]{ x, y };
    }

    private double[] getPlayerCoords(ICommandSender sender, String arg, GeographicProjection projection) {
        Vec3d pos = sender.getCommandSenderEntity().getPositionVector();
        try {
            return projection.toGeo(pos.x, pos.z);
        } catch (OutOfProjectionBoundsException e) { //out of bounds, return NaN
            return new double[]{ Double.NaN, Double.NaN };
        }
    }
}
