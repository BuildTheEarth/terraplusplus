package net.buildtheearth.terraminusminus.substitutes;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;


/**
 * Compatibility methods to translate between Terra-- internal Minecraft objects into Bukkit API objects.
 *
 * @author Smyler
 */
@UtilityClass
public final class TerraBukkit {

    /**
     * Translates internal Terra-- {@link Identifier} to Bukkit API's {@link NamespacedKey}.
     *
     * @param identifier the Terra-- {@link Identifier}
     * @return the Bukkit API {@link NamespacedKey}
     */
    public static @Nullable NamespacedKey toBukkitNamespacedKey(Identifier identifier) {
        if (identifier == null) return null;
        return NamespacedKey.fromString(identifier.toString());
    }

    /**
     * Translate Bukkit API's {@link NamespacedKey} to Terra-- internal {@link Identifier}.
     *
     * @param namespacedKey the Bukkit API {@link NamespacedKey}
     * @return the Terra-- {@link Identifier}
     */
    public static @Nullable Identifier fromBukkitNamespacedKey(NamespacedKey namespacedKey) {
        if (namespacedKey == null) return null;
        return new Identifier(namespacedKey.getNamespace(), namespacedKey.getKey());
    }

    /**
     * Translates internal Terra-- {@link BlockState blockstates} to Bukkit API's BlockData.
     * <br>
     * Takes advantage of an internal cache for optimized conversions.
     *
     * @param state the Terra-- {@link BlockState}
     * @return the Bukkit API {@link BlockData}
     */
    public static @Nullable BlockData toBukkitBlockData(@Nullable BlockState state) {
        if (state == null) return null;
        BlockStateBuilder.BlockStateImplementation implementation = null;
        if (state instanceof BlockStateBuilder.BlockStateImplementation) {
            BlockStateBuilder.BlockStateImplementation imp = (BlockStateBuilder.BlockStateImplementation) state;
            if (imp.bukkitBlockData != null) return (BlockData) imp.bukkitBlockData;
            implementation = imp;
        }
        Material material = Material.matchMaterial(state.getBlock().toString());
        if (material == null) return null;
        BlockData data = material.createBlockData(getPropertiesString(state));
        if (implementation != null) implementation.bukkitBlockData = data;
        return data;
    }

    /**
     * Translates Bukkit API {@link BlockData} to Terra-- internal {@link BlockState}.
     *
     * @param data the Bukkit API {@link BlockData}
     * @return the Terra-- {@link BlockState}
     */
    public static @Nullable BlockState fromBukkitBlockData(@Nullable BlockData data) {
        if (data == null) return null;
        String serializedData = data.getAsString();
        return BlockState.parse(serializedData);
    }

    private static String getPropertiesString(BlockState state) {
        return "[" + state.getProperties().entrySet().stream().map(
                    entry -> entry.getKey() + "=" + entry.getValue().getAsString()
                ).collect(Collectors.joining(",")) + ']';
    }

    /**
     * Translates Terra-- internal {@link Biome} into Bukkit API {@link org.bukkit.block.Biome}.
     *
     * @param biome the Terra-- {@link Biome}
     * @return the Bukkit API {@link org.bukkit.block.Biome}
     *
     * @apiNote this is best effort as 1.12.2 biomes do not match 1.17 biomes
     */
    public static @Nullable org.bukkit.block.Biome toBukkitBiome(@Nullable Biome biome) {
        if (biome == null) return null;
        switch (biome) {
            case OCEAN:
                return org.bukkit.block.Biome.OCEAN;
            case DESERT:
            case DESERT_HILLS:
            case MUTATED_DESERT:
                return org.bukkit.block.Biome.DESERT;
            case FOREST:
            case MUTATED_FOREST:
                return org.bukkit.block.Biome.FOREST;
            case RIVER:
                return org.bukkit.block.Biome.RIVER;
            case SAVANNA:
                return org.bukkit.block.Biome.SAVANNA;
            case BEACH:
                return org.bukkit.block.Biome.BEACH;
            case SKY:
                return org.bukkit.block.Biome.THE_END;
            case BIRCH_FOREST:
                return org.bukkit.block.Biome.BIRCH_FOREST;
            case HELL:
                return org.bukkit.block.Biome.NETHER_WASTES;
            case MESA:
            case MUTATED_MESA_CLEAR_ROCK:
                return org.bukkit.block.Biome.BADLANDS;
            case MUTATED_MESA:
                return org.bukkit.block.Biome.ERODED_BADLANDS;
            case JUNGLE:
            case JUNGLE_HILLS:
            case MUTATED_JUNGLE:
                    return org.bukkit.block.Biome.JUNGLE;
            case VOID:
                return org.bukkit.block.Biome.THE_VOID;
            case TAIGA:
            case TAIGA_HILLS:
            case MUTATED_TAIGA:
                return org.bukkit.block.Biome.TAIGA;
            case PLAINS:
                return org.bukkit.block.Biome.PLAINS;
            case MESA_ROCK:
            case MESA_CLEAR_ROCK:
            case MUTATED_MESA_ROCK:
                return org.bukkit.block.Biome.WOODED_BADLANDS;
            case SWAMPLAND:
            case MUTATED_SWAMPLAND:
                return org.bukkit.block.Biome.SWAMP;
            case COLD_BEACH:
                return org.bukkit.block.Biome.SNOWY_BEACH;
            case COLD_TAIGA:
            case COLD_TAIGA_HILLS:
            case MUTATED_TAIGA_COLD:
                return org.bukkit.block.Biome.SNOWY_TAIGA;
            case DEEP_OCEAN:
                return org.bukkit.block.Biome.DEEP_OCEAN;
            case ICE_PLAINS:
                return org.bukkit.block.Biome.SNOWY_PLAINS;
            case JUNGLE_EDGE:
            case MUTATED_JUNGLE_EDGE:
                return org.bukkit.block.Biome.SPARSE_JUNGLE;
            case STONE_BEACH:
                return org.bukkit.block.Biome.STONY_SHORE;
            case FOREST_HILLS:
            case EXTREME_HILLS_WITH_TREES:
                return org.bukkit.block.Biome.WINDSWEPT_FOREST;
            case FROZEN_OCEAN:
                return org.bukkit.block.Biome.FROZEN_OCEAN;
            case FROZEN_RIVER:
                return org.bukkit.block.Biome.FROZEN_RIVER;
            case EXTREME_HILLS:
            case EXTREME_HILLS_EDGE:
                return org.bukkit.block.Biome.WINDSWEPT_HILLS;
            case ICE_MOUNTAINS:
                return org.bukkit.block.Biome.FROZEN_PEAKS;
            case REDWOOD_TAIGA:
            case REDWOOD_TAIGA_HILLS:
            case MUTATED_REDWOOD_TAIGA:
            case MUTATED_REDWOOD_TAIGA_HILLS:
                return org.bukkit.block.Biome.OLD_GROWTH_SPRUCE_TAIGA;
            case ROOFED_FOREST:
            case MUTATED_ROOFED_FOREST:
                return org.bukkit.block.Biome.DARK_FOREST;
            case MUTATED_PLAINS:
                return org.bukkit.block.Biome.SUNFLOWER_PLAINS;
            case MUSHROOM_ISLAND:
            case MUSHROOM_ISLAND_SHORE:
                return org.bukkit.block.Biome.MUSHROOM_FIELDS;
            case MUTATED_SAVANNA:
            case MUTATED_SAVANNA_ROCK:
                return org.bukkit.block.Biome.WINDSWEPT_SAVANNA;
            case SAVANNA_PLATEAU:
                return org.bukkit.block.Biome.SAVANNA_PLATEAU;
            case MUTATED_ICE_FLATS:
                return org.bukkit.block.Biome.ICE_SPIKES;
            case BIRCH_FOREST_HILLS:
            case MUTATED_BIRCH_FOREST:
            case MUTATED_BIRCH_FOREST_HILLS:
                return org.bukkit.block.Biome.OLD_GROWTH_BIRCH_FOREST;
            case MUTATED_EXTREME_HILLS:
            case MUTATED_EXTREME_HILLS_WITH_TREES:
                return org.bukkit.block.Biome.WINDSWEPT_GRAVELLY_HILLS;
            case UNKNOWN:
                return org.bukkit.block.Biome.CUSTOM;
            default:
                throw new IllegalStateException("Unknown internal biome: " + biome);
        }
    }

    /**
     * Translates Bukkit {@link org.bukkit.block.Biome} into Terra-- internal {@link Biome}.
     *
     * @param biome the Bukkit {@link org.bukkit.block.Biome}
     * @return the Terra-- {@link Biome}
     *
     * @apiNote this is best effort as 1.12.2 biomes do not match 1.17 biomes
     */
    public static Biome fromBukkitBiome(org.bukkit.block.Biome biome) {
        switch (biome) {
            case OCEAN:
                return Biome.OCEAN;
            case PLAINS:
            case DRIPSTONE_CAVES:
            case LUSH_CAVES:
            case MEADOW:
                return Biome.PLAINS;
            case DESERT:
                return Biome.DESERT;
            case WINDSWEPT_HILLS:
            case JAGGED_PEAKS:
                return Biome.EXTREME_HILLS;
            case FOREST:
                return Biome.FOREST;
            case TAIGA:
                return Biome.TAIGA;
            case SWAMP:
                return Biome.SWAMPLAND;
            case RIVER:
                return Biome.RIVER;
            case NETHER_WASTES:
            case SOUL_SAND_VALLEY:
            case CRIMSON_FOREST:
            case WARPED_FOREST:
            case BASALT_DELTAS:
                return Biome.HELL;
            case THE_END:
            case SMALL_END_ISLANDS:
            case END_MIDLANDS:
            case END_HIGHLANDS:
            case END_BARRENS:
                return Biome.SKY;
            case FROZEN_OCEAN:
            case COLD_OCEAN:
            case DEEP_COLD_OCEAN:
            case DEEP_FROZEN_OCEAN:
                return Biome.FROZEN_OCEAN;
            case FROZEN_RIVER:
                return Biome.FROZEN_RIVER;
            case SNOWY_PLAINS:
                return Biome.ICE_PLAINS;
            case MUSHROOM_FIELDS:
                return Biome.MUSHROOM_ISLAND;
            case BEACH:
                return Biome.BEACH;
            case JUNGLE:
            case BAMBOO_JUNGLE:
                return Biome.JUNGLE;
            case SPARSE_JUNGLE:
                return Biome.JUNGLE_EDGE;
            case DEEP_OCEAN:
            case WARM_OCEAN:
            case LUKEWARM_OCEAN:
            case DEEP_LUKEWARM_OCEAN:
                return Biome.DEEP_OCEAN;
            case STONY_SHORE:
                return Biome.STONE_BEACH;
            case SNOWY_BEACH:
                return Biome.COLD_BEACH;
            case BIRCH_FOREST:
                return Biome.BIRCH_FOREST;
            case DARK_FOREST:
                return Biome.ROOFED_FOREST;
            case SNOWY_TAIGA:
                return Biome.COLD_TAIGA_HILLS;
            case OLD_GROWTH_PINE_TAIGA:
                return Biome.REDWOOD_TAIGA_HILLS;
            case WINDSWEPT_FOREST:
                return Biome.EXTREME_HILLS_WITH_TREES;
            case SAVANNA:
                return Biome.SAVANNA;
            case SAVANNA_PLATEAU:
                return Biome.SAVANNA_PLATEAU;
            case BADLANDS:
            case WOODED_BADLANDS:
                return Biome.MESA;
            case THE_VOID:
                return Biome.VOID;
            case SUNFLOWER_PLAINS:
                return Biome.MUTATED_PLAINS;
            case WINDSWEPT_GRAVELLY_HILLS:
                return Biome.EXTREME_HILLS_EDGE;
            case FLOWER_FOREST:
                return Biome.FOREST_HILLS;
            case ICE_SPIKES:
                return Biome.MUTATED_ICE_FLATS;
            case OLD_GROWTH_BIRCH_FOREST:
                return Biome.BIRCH_FOREST_HILLS;
            case OLD_GROWTH_SPRUCE_TAIGA:
                return Biome.MUTATED_TAIGA;
            case WINDSWEPT_SAVANNA:
                return Biome.MUTATED_SAVANNA_ROCK;
            case ERODED_BADLANDS:
                return Biome.MUTATED_MESA;
            case GROVE:
                return Biome.COLD_TAIGA;
            case SNOWY_SLOPES:
            case FROZEN_PEAKS:
            case STONY_PEAKS:
                return Biome.ICE_MOUNTAINS;
            case CUSTOM:
            default:
                return Biome.UNKNOWN;
        }
    }

}
