package net.buildtheearth.terraminusminus.substitutes;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.stream.Collectors;

/**
 * Makes life easier when working with Bukkit and Terra--
 *
 * @author SmylerMC
 */
public class BukkitBindings {

    public static BlockData getAsBlockData(BlockState state, boolean legacy) {
        if (state == null) return null;
        BlockStateBuilder.BlockStateImplementation implementation = null;
        if (state instanceof BlockStateBuilder.BlockStateImplementation imp) {
            if (imp.bukkitBlockData != null) return (BlockData) imp.bukkitBlockData;
            implementation = imp;
        }
        Material material = Material.getMaterial(state.getBlock().toString(), legacy);
        if (material == null) return null;
        BlockData data = material.createBlockData(getPropertiesString(state));
        if (implementation != null) implementation.bukkitBlockData = data;
        return data;
    }

    public static BlockData getAsBlockData(BlockState state) {
        return getAsBlockData(state, false);
    }

    private static String getPropertiesString(BlockState state) {
        return "[" + state.getProperties().entrySet().stream().map(
                    entry -> entry.getKey() + "=" + entry.getValue().getAsString()
                ).collect(Collectors.joining(",")) + ']';
    }

}
