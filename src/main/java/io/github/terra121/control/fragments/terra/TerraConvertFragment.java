package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.TerraConstants;
import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.TranslateUtil;
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

        if(args.length < 2) {
            sender.sendMessage(ChatHelper.makeTextComponent(new TextElement("Usage: /terra convert <x/lat> <z/lon>", TextFormatting.RED)));
            return;
        }

        double x;
        double y;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
        } catch (Exception e) {
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.numbers"), TextFormatting.RED)));
            return;
        }


        double[] c = new double[]{x, y};

        try {
            if (-180 <= c[1] && c[1] <= 180 && -90 <= c[0] && c[0] <= 90) {
                c = projection.fromGeo(c[1], c[0]);
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Result: ", TextFormatting.GRAY), new TextElement("" + c[0], TextFormatting.BLUE),
                        new TextElement(", ", TextFormatting.GRAY), new TextElement("" + c[1], TextFormatting.BLUE)));
            } else {
                c = projection.toGeo(c[0], c[1]);
                sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Result: ", TextFormatting.GRAY), new TextElement("" + c[1], TextFormatting.BLUE),
                        new TextElement(", ", TextFormatting.GRAY), new TextElement("" + c[0], TextFormatting.BLUE)));
            }
        } catch (OutOfProjectionBoundsException e) { //out of bounds, print error
            sender.sendMessage(ChatHelper.makeTitleTextComponent(new TextElement("Invalid coordinats!", TextFormatting.RED)));
        }
    }

    @Override
    public String[] getName() {
        return new String[]{"convert", "conv"};
    }

    @Override
    public String getPurpose() {
        return TranslateUtil.translate("terra121.fragment.terra.convert.purpose");
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
