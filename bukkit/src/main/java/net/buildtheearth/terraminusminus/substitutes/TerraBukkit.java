package net.buildtheearth.terraminusminus.substitutes;

import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.substitutes.exceptions.TranslateToForeignObjectException;
import net.buildtheearth.terraminusminus.substitutes.exceptions.TranslateToSubstituteException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static java.lang.Double.*;
import static org.bukkit.Location.*;


/**
 * Compatibility methods to translate between Terra-- internal Minecraft objects into Bukkit API objects.
 *
 * @author Smyler
 */
@UtilityClass
public final class TerraBukkit {

    /**
     * Translates internal Terra-- {@link Identifier} to Bukkit API's {@link NamespacedKey}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link NamespacedKey} for each call.
     *
     * @param identifier the Terra-- {@link Identifier}
     * @return the Bukkit API {@link NamespacedKey}
     */
    @Contract("!null -> !null; null -> null")
    public static @Nullable NamespacedKey toBukkitNamespacedKey(@Nullable Identifier identifier) {
        if (identifier == null) return null;
        NamespacedKey namespacedKey = NamespacedKey.fromString(identifier.toString());
        if (namespacedKey == null) {
            throw new IllegalStateException("Identifier conversion to Bukkit NamespacedKey should never fail");
        }
        return namespacedKey;
    }

    /**
     * Translate Bukkit API's {@link NamespacedKey} to Terra-- internal {@link Identifier}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link Identifier} for each call.
     *
     * @param namespacedKey the Bukkit API {@link NamespacedKey}
     * @return the Terra-- {@link Identifier}
     */
    @Contract("!null -> new; null -> null")
    public static @Nullable Identifier fromBukkitNamespacedKey(@Nullable NamespacedKey namespacedKey) {
        if (namespacedKey == null) return null;
        return new Identifier(namespacedKey.getNamespace(), namespacedKey.getKey());
    }

    /**
     * Translates an internal Terra-- {@link BlockPos} to Bukkit API's {@link Location}.
     * Conserves nullness.
     * <br>
     * The {@link Location} world is <code>null</code>, yaw and pitch are 0.
     * <br>
     * This method has no form of caching and may create a new instance of {@link Location} for each call.
     *
     * @param blockPos the Terra-- {@link BlockPos}
     * @return the Bukkit API {@link Location}
     */
    @Contract("!null -> new; null -> null")
    public static @Nullable Location toBukkitLocation(@Nullable BlockPos blockPos) {
        if (blockPos == null) return null;
        return new Location(null, blockPos.x, blockPos.y, blockPos.z, 0f, 0f);
    }

    /**
     * Translate a Bukkit API {@link Location} to an internal Terra-- {@link BlockPos}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link BlockPos} for each call.
     *
     * @param location the Bukkit API {@link Location}
     * @return the internal Terra-- {@link BlockPos}
     *
     * @throws TranslateToSubstituteException if one of the {@link Location location}'s x, y or z components is not a finite double
     */
    @Contract("!null -> !null; null -> null")
    public static @Nullable BlockPos fromBukkitLocation(@Nullable Location location) throws TranslateToSubstituteException {
        if (location == null) return null;
        double x = location.getX(), y = location.getY(), z = location.getZ();
        if (!isFinite(x) || !isFinite(y) || !isFinite(z)) {
            throw new TranslateToSubstituteException(location, BlockPos.class, "one of the x, y, or z components is not finite");
        }
        return new BlockPos(locToBlock(x), locToBlock(y), locToBlock(z));
    }

    /**
     * Translates internal Terra-- {@link BlockState blockstates} to Bukkit API's BlockData.
     * Conserves nullness.
     * <br>
     * If the {@link BlockState} implementation is the canonical Terra-- one,
     * takes advantage of an internal cache for optimized conversions.
     *
     * @param state the Terra-- {@link BlockState}
     * @return the Bukkit API {@link BlockData}
     *
     * @throws TranslateToForeignObjectException if the block state is invalid to the Bukkit implementation
     * (e.g. it is from newer Minecraft versions and the block does not yet exist in the Bukkit server version).
     */
    @Contract("!null -> !null; null -> null")
    public static @Nullable BlockData toBukkitBlockData(@Nullable BlockState state) throws TranslateToForeignObjectException {
        if (state == null) return null;
        BlockStateBuilder.BlockStateImplementation implementation = null;
        if (state instanceof BlockStateBuilder.BlockStateImplementation) {
            BlockStateBuilder.BlockStateImplementation imp = (BlockStateBuilder.BlockStateImplementation) state;
            if (imp.bukkitBlockData != null) return (BlockData) imp.bukkitBlockData;
            implementation = imp;
        }
        Material material = Material.matchMaterial(state.getBlock().toString());
        if (material == null) {
            throw new TranslateToForeignObjectException(
                    state, BlockData.class, "material is unknown to server"
            );
        }
        BlockData data;
        try {
            data = material.createBlockData(BlockStateBuilder.BlockStateImplementation.formatProperties(state));
        } catch (IllegalArgumentException e) {
            throw new TranslateToForeignObjectException(
                    state, BlockData.class, "server reported block state properties as invalid"
            );
        }
        if (implementation != null)
            implementation.bukkitBlockData = data;
        return data;
    }

    /**
     * Translates Bukkit API {@link BlockData} to Terra-- internal {@link BlockState}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link BlockData} for each call.
     *
     * @param data the Bukkit API {@link BlockData}
     * @return the Terra-- {@link BlockState}
     */
    @Contract("!null -> !null; null -> null")
    public static @Nullable BlockState fromBukkitBlockData(@Nullable BlockData data) {
        if (data == null) return null;
        String serializedData = data.getAsString();
        return BlockState.parse(serializedData);  // The parse here should never fail
    }

    /**
     * Translates Terra-- internal {@link Biome} into Bukkit API {@link org.bukkit.block.Biome}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link org.bukkit.block.Biome} for each call.
     *
     * @param biome the Terra-- {@link Biome}
     * @return the Bukkit API {@link org.bukkit.block.Biome}
     *
     * @throws TranslateToForeignObjectException if the biome is unknown to the Bukkit implementation
     * (e.g. it is from newer Minecraft versions and the biome does not yet exist in the Bukkit server version).
     */
    @Contract("!null -> !null; null -> null")
    public static @Nullable org.bukkit.block.Biome toBukkitBiome(@Nullable Biome biome) {
        if (biome == null) return null;
        org.bukkit.block.Biome bukkitBiome = Registry.BIOME.get(toBukkitNamespacedKey(biome.identifier()));
        if (bukkitBiome == null) {
            throw new TranslateToForeignObjectException(
                    biome, org.bukkit.block.Biome.class, "biome is unknown to server"
            );
        }
        return bukkitBiome;
    }

    /**
     * Translates Bukkit {@link org.bukkit.block.Biome} into Terra-- internal {@link Biome}.
     * Conserves nullness.
     * <br>
     * This method has no form of caching and may create a new instance of {@link Biome} for each call.
     *
     * @param biome the Bukkit {@link org.bukkit.block.Biome}
     * @return the Terra-- {@link Biome}
     */
    @Contract("!null -> !null; null -> null")
    public static Biome fromBukkitBiome(org.bukkit.block.Biome biome) {
        if (biome == null) return null;
        return Biome.byId(fromBukkitNamespacedKey(biome.getKey()));
    }

}
