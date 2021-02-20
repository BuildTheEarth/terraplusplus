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

public class TerraConvertFragment extends CommandFragment{
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

        if(args.length < 2) {
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, "Usage: /terra convert <x/lat> <z/lon>"));
            return;
        }

        double x;
        double y;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
        } catch (Exception e) {
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MODID + ".error.numbers")));
            return;
        }


        double[] c = new double[]{x, y};

        if (-180 <= c[1] && c[1] <= 180 && -90 <= c[0] && c[0] <= 90) {
            try {
                c = projection.fromGeo(c[1], c[0]);
            } catch (OutOfProjectionBoundsException e) {
                e.printStackTrace();
            }
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Result: ",
                    TextFormatting.BLUE, c[0], TextFormatting.GRAY, ", ", TextFormatting.BLUE, c[1]));
        } else {
            try {
                c = projection.toGeo(c[0], c[1]);
            } catch (OutOfProjectionBoundsException e) {
                e.printStackTrace();
            }
            sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Result: ",
                    TextFormatting.BLUE, c[1], TextFormatting.GRAY, ", ", TextFormatting.BLUE, c[0]));
        }
    }

    @Override
    public String[] getName() {
        return new String[]{"convert", "conv"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate(TerraConstants.MODID + ".fragment.terra.convert.purpose").getUnformattedComponentText();
    }

    @Override
    public String[] getArguments() {
        return new String[]{"<x/lat>", "<z/lon>"};
    }

    @Override
    public String getPermission() {
        return null;
    }
}
