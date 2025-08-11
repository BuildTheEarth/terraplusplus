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

    OCEAN(0, "ocean"),
    PLAINS(1, "plains"),
    DESERT(2, "desert"),
    EXTREME_HILLS(3, "extreme_hills"),
    FOREST(4, "forest"),
    TAIGA(5, "taiga"),
    SWAMPLAND(6, "swampland"),
    RIVER(7, "river"),
    HELL(8, "hell"),
    SKY(9, "sky"),
    FROZEN_OCEAN(10, "frozen_ocean"),
    FROZEN_RIVER(11, "frozen_river"),
    ICE_PLAINS(12, "ice_flats"),
    ICE_MOUNTAINS(13, "ice_mountains"),
    MUSHROOM_ISLAND(14, "mushroom_island"),
    MUSHROOM_ISLAND_SHORE(15, "mushroom_island_shore"),
    BEACH(16, "beaches"),
    DESERT_HILLS(17, "desert_hills"),
    FOREST_HILLS(18, "forest_hills"),
    TAIGA_HILLS(19, "taiga_hills"),
    EXTREME_HILLS_EDGE(20, "smaller_extreme_hills"),
    JUNGLE(21, "jungle"),
    JUNGLE_HILLS(22, "jungle_hills"),
    JUNGLE_EDGE(23, "jungle_edge"),
    DEEP_OCEAN(24, "deep_ocean"),
    STONE_BEACH(25, "stone_beach"),
    COLD_BEACH(26, "cold_beach"),
    BIRCH_FOREST(27, "birch_forest"),
    BIRCH_FOREST_HILLS(28, "birch_forest_hills"),
    ROOFED_FOREST(29, "roofed_forest"),
    COLD_TAIGA(30, "taiga_cold"),
    COLD_TAIGA_HILLS(31, "taiga_cold_hills"),
    REDWOOD_TAIGA(32, "redwood_taiga"),
    REDWOOD_TAIGA_HILLS(33, "redwood_taiga_hills"),
    EXTREME_HILLS_WITH_TREES(34, "extreme_hills_with_trees"),
    SAVANNA(35, "savanna"),
    SAVANNA_PLATEAU(36, "savanna_rock"),
    MESA(37, "mesa"),
    MESA_ROCK(38, "mesa_rock"),
    MESA_CLEAR_ROCK(39, "mesa_clear_rock"),
    VOID(40, "void"),
    MUTATED_PLAINS(41, "mutated_plains"),
    MUTATED_DESERT(42, "mutated_desert"),
    MUTATED_EXTREME_HILLS(43, "mutated_extreme_hills"),
    MUTATED_FOREST(44, "mutated_forest"),
    MUTATED_TAIGA(45, "mutated_taiga"),
    MUTATED_SWAMPLAND(46, "mutated_swampland"),
    MUTATED_ICE_FLATS(47, "mutated_ice_flats"),
    MUTATED_JUNGLE(48, "mutated_jungle"),
    MUTATED_JUNGLE_EDGE(49, "mutated_jungle_edge"),
    MUTATED_BIRCH_FOREST(50, "mutated_birch_forest"),
    MUTATED_BIRCH_FOREST_HILLS(51, "mutated_birch_forest_hills"),
    MUTATED_ROOFED_FOREST(52, "mutated_roofed_forest"),
    MUTATED_TAIGA_COLD(53, "mutated_taiga_cold"),
    MUTATED_REDWOOD_TAIGA(54, "mutated_redwood_taiga"),
    MUTATED_REDWOOD_TAIGA_HILLS(55, "mutated_redwood_taiga_hills"),
    MUTATED_EXTREME_HILLS_WITH_TREES(56, "mutated_extreme_hills_with_trees"),
    MUTATED_SAVANNA(57, "mutated_savanna"),
    MUTATED_SAVANNA_ROCK(58, "mutated_savanna_rock"),
    MUTATED_MESA(59, "mutated_mesa"),
    MUTATED_MESA_ROCK(60, "mutated_mesa_rock"),
    MUTATED_MESA_CLEAR_ROCK(61, "mutated_mesa_clear_rock"),
    UNKNOWN(-1, "unknown");
    
    public final String biomeId;
    public final int numericId;
    
    Biome(int numericId, String biomeId) {
    	this.biomeId = biomeId;
    	this.numericId = numericId;
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
