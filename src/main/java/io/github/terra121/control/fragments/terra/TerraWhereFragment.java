package io.github.terra121.control.fragments.terra;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.terra121.control.fragments.CommandFragment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CardinalDirection;
import io.github.terra121.util.ChatUtil;
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

		if (!(gen instanceof EarthGenerator)) {
			sender.sendMessage(ChatUtil.getNotTerra());
			return;
		}

		Vec3d pos = sender.getPositionVector();
		Entity e = sender.getCommandSenderEntity();
		String senderName = sender.getName();
		float yaw = e.rotationYaw;
		if (args.length > 0) {
			if(hasAdminPermission(sender)) e = sender.getEntityWorld().getPlayerEntityByName(args[0]);
			if (e == null) {
				sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.unknownplayer")));
				return;
			}

			pos = e.getPositionVector();
			senderName = e.getName();
			yaw = e.rotationYaw;
		}

		EarthGenerator terrain = (EarthGenerator) gen;
		GeographicProjection projection = terrain.projection;


		double[] result;
		float azimuth;
		try {
			result = projection.toGeo(pos.x, pos.z);
			azimuth = projection.azimuth(pos.x, pos.z, yaw);
		} catch (OutOfProjectionBoundsException e1) { //out of bounds, set to null to print error
			result = null;
			azimuth = Float.NaN;
		}
		sender.sendMessage(ChatUtil.titleAndCombine(TextFormatting.GRAY, "Location of ", TextFormatting.BLUE, senderName));
		if (result == null || Double.isNaN(result[0])) {
			sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.fragment.terra.where.notproj")));
			return;
		}
		if (!Float.isFinite(azimuth)) {
			sender.sendMessage(ChatUtil.combine(TextFormatting.RED, TranslateUtil.translate("terra121.fragment.terra.where.notproj")));
			return;
		}
		sender.sendMessage(ChatUtil.combine(TextFormatting.GRAY, "Location: ", TextFormatting.BLUE, result[1],
				TextFormatting.GRAY, ", ", TextFormatting.BLUE, result[0], TextFormatting.GRAY, " Facing: ", TextFormatting.BLUE, CardinalDirection.azimuthToFacing(azimuth).realName(), TextFormatting.GRAY, " (", TextFormatting.BLUE, azimuth, TextFormatting.GRAY, ")"));
	}

	@Override
	public String[] getName() {
		return new String[]{"where", "ou"};
	}

	@Override
	public String getPurpose() {
		return TranslateUtil.translate("terra121.fragment.terra.where.purpose").getUnformattedComponentText();
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
