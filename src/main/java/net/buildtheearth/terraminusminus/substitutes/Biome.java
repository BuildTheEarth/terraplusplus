package net.buildtheearth.terraminusminus.substitutes;

/**
 * All vanilla Minecraft 1.12.2 biomes.
 *
 * Terra++'s biomes are not data-driven yet, so this is a simple enum.
 * 
 * @author SmylerMC
 *
 */
public enum Biome {

    OCEAN("ocean"),
    PLAINS("plains"),
    DESERT("desert"),
    EXTREME_HILLS("extreme_hills"),
    FOREST("forest"),
    TAIGA("taiga"),
    SWAMPLAND("swampland"),
    RIVER("river"),
    HELL("hell"),
    SKY("sky"),
    FROZEN_OCEAN("frozen_ocean"),
    FROZEN_RIVER("frozen_river"),
    ICE_PLAINS("ice_flats"),
    ICE_MOUNTAINS("ice_mountains"),
    MUSHROOM_ISLAND("mushroom_island"),
    MUSHROOM_ISLAND_SHORE("mushroom_island_shore"),
    BEACH("beaches"),
    DESERT_HILLS("desert_hills"),
    FOREST_HILLS("forest_hills"),
    TAIGA_HILLS("taiga_hills"),
    EXTREME_HILLS_EDGE("smaller_extreme_hills"),
    JUNGLE("jungle"),
    JUNGLE_HILLS("jungle_hills"),
    JUNGLE_EDGE("jungle_edge"),
    DEEP_OCEAN("deep_ocean"),
    STONE_BEACH("stone_beach"),
    COLD_BEACH("cold_beach"),
    BIRCH_FOREST("birch_forest"),
    BIRCH_FOREST_HILLS("birch_forest_hills"),
    ROOFED_FOREST("roofed_forest"),
    COLD_TAIGA("taiga_cold"),
    COLD_TAIGA_HILLS("taiga_cold_hills"),
    REDWOOD_TAIGA("redwood_taiga"),
    REDWOOD_TAIGA_HILLS("redwood_taiga_hills"),
    EXTREME_HILLS_WITH_TREES("extreme_hills_with_trees"),
    SAVANNA("savanna"),
    SAVANNA_PLATEAU("savanna_rock"),
    MESA("mesa"),
    MESA_ROCK("mesa_rock"),
    MESA_CLEAR_ROCK("mesa_clear_rock"),
    VOID("void"),
    MUTATED_PLAINS("mutated_plains"),
    MUTATED_DESERT("mutated_desert"),
    MUTATED_EXTREME_HILLS("mutated_extreme_hills"),
    MUTATED_FOREST("mutated_forest"),
    MUTATED_TAIGA("mutated_taiga"),
    MUTATED_SWAMPLAND("mutated_swampland"),
    MUTATED_ICE_FLATS("mutated_ice_flats"),
    MUTATED_JUNGLE("mutated_jungle"),
    MUTATED_JUNGLE_EDGE("mutated_jungle_edge"),
    MUTATED_BIRCH_FOREST("mutated_birch_forest"),
    MUTATED_BIRCH_FOREST_HILLS("mutated_birch_forest_hills"),
    MUTATED_ROOFED_FOREST("mutated_roofed_forest"),
    MUTATED_TAIGA_COLD("mutated_taiga_cold"),
    MUTATED_REDWOOD_TAIGA("mutated_redwood_taiga"),
    MUTATED_REDWOOD_TAIGA_HILLS("mutated_redwood_taiga_hills"),
    MUTATED_EXTREME_HILLS_WITH_TREES("mutated_extreme_hills_with_trees"),
    MUTATED_SAVANNA("mutated_savanna"),
    MUTATED_SAVANNA_ROCK("mutated_savanna_rock"),
    MUTATED_MESA("mutated_mesa"),
    MUTATED_MESA_ROCK("mutated_mesa_rock"),
    MUTATED_MESA_CLEAR_ROCK("mutated_mesa_clear_rock"),
    UNKNOWN("unknown");

    public final String biomeId;

    Biome(String biomeId) {
    	this.biomeId = biomeId;
    }
    
    public static Biome byId(String biomeId) {
    	for(Biome b: values()) {
    		if(b.biomeId.equals(biomeId)) return b;
    	}
    	return null;
    }
    
    public static Biome getDefault() {
    	return OCEAN;
    }

}
