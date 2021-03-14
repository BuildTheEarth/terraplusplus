package net.buildtheearth.terraminusminus.substitutes.net.minecraft.block;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.properties.IProperty;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.state.IBlockState;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.ResourceLocation;

public enum Block {

	AIR(0, "air"),
    STONE(1, "stone"),
    GRASS(2, "grass"),
    DIRT(3, "dirt"),
    COBBLESTONE(4, "cobblestone"),
    PLANKS(5, "planks"),
    SAPLING(6, "sapling"),
    BEDROCK(7, "bedrock"),
    FLOWING_WATER(8, "flowing_water"),
    WATER(9, "water"),
    FLOWING_LAVA(10, "flowing_lava"),
    LAVA(11, "lava"),
    SAND(12, "sand"),
    GRAVEL(13, "gravel"),
    GOLD_ORE(14, "gold_ore"),
    IRON_ORE(15, "iron_ore"),
    COAL_ORE(16, "coal_ore"),
    LOG(17, "log"),
    LEAVES(18, "leaves"),
    SPONGE(19, "sponge"),
    GLASS(20, "glass"),
    LAPIS_ORE(21, "lapis_ore"),
    LAPIS_BLOCK(22, "lapis_block"),
    DISPENSER(23, "dispenser"),
    SANDSTONE(24, "sandstone"),
    NOTEBLOCK(25, "noteblock"),
    BED(26, "bed"),
    GOLDEN_RAIL(27, "golden_rail"),
    DETECTOR_RAIL(28, "detector_rail"),
    STICK_PISTON(29, "sticky_piston"),
    WEB(30, "web"),
    TALLGRASS(31, "tallgrass"),
    DEADBUSH(32, "deadbush"),
    PISTON(33, "piston"),
    PISTON_HEAD(34, "piston_head"),
    WOOL(35, "wool"),
    PISTON_EXTENSION(36, "piston_extension"),
    YELLOW_FLOWER(37, "yellow_flower"),
    RED_FLOWER(38, "red_flower"),
    BROWN_MUSHROOM(39, "brown_mushroom"),
    RED_MUSHROOM(40, "red_mushroom"),
    GLOLD_BLOCK(41, "gold_block"),
    IRON_BLOCK(42, "iron_block"),
    DOUBLE_STONE_SLAB(43, "double_stone_slab"),
    SONT_SLAB(44, "stone_slab"),
    BRICK_BLOCK(45, "brick_block"),
    TNT(46, "tnt"),
    BOOKSHELF(47, "bookshelf"),
    MOSSY_COBBLESTON(48, "mossy_cobblestone"),
    OBSIDIAN(49, "obsidian"),
    TORCH(50, "torch"),
    FIRE(51, "fire"),
    MOB_SPAWNER(52, "mob_spawner"),
    OAK_STAIRS(53, "oak_stairs"),
    CHEST(54, "chest"),
    REDSTONE_WIRE(55, "redstone_wire"),
    DIAMOND_ORE(56, "diamond_ore"),
    DIAMOND_BLOCK(57, "diamond_block"),
    CRAFTING_TABLE(58, "crafting_table"),
    WHEAT(59, "wheat"),
    FARMLAND(60, "farmland"),
    FURNACE(61, "furnace"),
    LIT_FURNACE(62, "lit_furnace"),
    STANDING_SIGN(63, "standing_sign"),
    WOODEN_DOOR(64, "wooden_door"),
    LADDER(65, "ladder"),
    RAIL(66, "rail"),
    STONE_STAIRS(67, "stone_stairs"),
    WALL_SIGN(68, "wall_sign"),
    LEVER(69, "lever"),
    STONE_PRESSURE_PLATE(70, "stone_pressure_plate"),
    IRON_DOOR(71, "iron_door"),
    WOODEN_PRESSURE_PLATE(72, "wooden_pressure_plate"),
    REDSTON_ORE(73, "redstone_ore"),
    LIT_REDTONE_ORE(74, "lit_redstone_ore"),
    UNLIT_REDSTONE_TORCH(75, "unlit_redstone_torch"),
    REDSTONE_TORCH(76, "redstone_torch"),
    STONE_BUTTON(77, "stone_button"),
    SNOW_LAYER(78, "snow_layer"),
    ICE(79, "ice"),
    SNOW(80, "snow"),
    CACTUS(81, "cactus"),
    CLAY(82, "clay"),
    REEDS(83, "reeds"),
    JUKEBOX(84, "jukebox"),
    FENCE(85, "fence"),
    PUMPIN(86, "pumpkin"),
    NETHERRACK(87, "netherrack"),
    SOUL_SAND(88, "soul_sand"),
    GLOWSTONE(89, "glowstone"),
    PORTAL(90, "portal"),
    LIT_PUMPIN(91, "lit_pumpkin"),
    CAKE(92, "cake"),
    UNPOWERED_REPEATER(93, "unpowered_repeater"),
    POWERED_REPEATER(94, "powered_repeater"),
    STAINED_GLASS(95, "stained_glass"),
    TRAPDOOR(96, "trapdoor"),
    MONSTER_EGG(97, "monster_egg"),
    STONEBRICK(98, "stonebrick"),
    BROWN_MUSHROOM_BLOCK(99, "brown_mushroom_block"),
    RED_MUSHROOM_BLOCK(100, "red_mushroom_block"),
    IRONS_BARS(101, "iron_bars"),
    GLASS_PANE(102, "glass_pane"),
    MELON_BLOCK(103, "melon_block"),
    PUMPIN_STEM(104, "pumpkin_stem"),
    MELON_STEM(105, "melon_stem"),
    VINE(106, "vine"),
    FENCE_GATE(107, "fence_gate"),
    BRICK_STAIRS(108, "brick_stairs"),
    STONE_BRICK_STAIRS(109, "stone_brick_stairs"),
    MYCELIUM(110, "mycelium"),
    WATERLILY(111, "waterlily"),
    NETHER_BRICK(112, "nether_brick"),
    NETHER_BRICK_FENCE(113, "nether_brick_fence"),
    NETHER_BRICK_STAIRS(114, "nether_brick_stairs"),
    NETHER_WART(115, "nether_wart"),
    ENCHANTING_TABLE(116, "enchanting_table"),
    BREWING_STAND(117, "brewing_stand"),
    CAULDRON(118, "cauldron"),
    END_PORTAL(119, "end_portal"),
    END_PORTAL_FRAME(120, "end_portal_frame"),
    END_STONE(121, "end_stone"),
    DRAGON_EGG(122, "dragon_egg"),
    REDSTONE_LAMP(123, "redstone_lamp"),
    LIT_REDSTONE_LAMP(124, "lit_redstone_lamp"),
    DOUBLE_WOODEN_SLAB(125, "double_wooden_slab"),
    WOODEN_SLAB(126, "wooden_slab"),
    COCOA(127, "cocoa"),
    SANDSTONE_STAIRS(128, "sandstone_stairs"),
    EMARALD_ORE(129, "emerald_ore"),
    ENDER_CHEST(130, "ender_chest"),
    TRIPWIRE_HOOK(131, "tripwire_hook"),
    TRIPWIRE(132, "tripwire"),
    EMERALD_BLOCK(133, "emerald_block"),
    SPRUCE_STAIRS(134, "spruce_stairs"),
    BIRCH_STAIRS(135, "birch_stairs"),
    JUNGLE_STAIRS(136, "jungle_stairs"),
    COMMAND_BLOCK(137, "command_block"),
    BEACON(138, "beacon"),
    COBBLESTONE_WALL(139, "cobblestone_wall"),
    FLOWER_POT(140, "flower_pot"),
    CARROTS(141, "carrots"),
    POTATOES(142, "potatoes"),
    WOODEN_BUTTON(143, "wooden_button"),
    SKULL(144, "skull"),
    ANVIL(145, "anvil"),
    TRAPPED_CHEST(146, "trapped_chest"),
    LIGHT_WEIGHTED_PRESSURE_PLATE(147, "light_weighted_pressure_plate"),
    HEAVY_WEIGHTED_PRESSURE_PLATE(148, "heavy_weighted_pressure_plate"),
    UNPOWERED_COMPARATOR(149, "unpowered_comparator"),
    POWERED_COMPARATOR(150, "powered_comparator"),
    DAYLIGHT_DETECTOR(151, "daylight_detector"),
    REDSTONE_BLOCK(152, "redstone_block"),
    QUARTZ_ORE(153, "quartz_ore"),
    HOPPER(154, "hopper"),
    QUARTZ_BLOCK(155, "quartz_block"),
    QUARTZ_STAIRS(156, "quartz_stairs"),
    ACTIVATOR_RAIL(157, "activator_rail"),
    DROPPER(158, "dropper"),
    STAINED_HARDENED_CLAY(159, "stained_hardened_clay"),
    STAINED_GLASS_PANE(160, "stained_glass_pane"),
    LEAVES2(161, "leaves2"),
    LOG2(162, "log2"),
    ACACIA_STAIRS(163, "acacia_stairs"),
    DARK_OAK_STAIRS(164, "dark_oak_stairs"),
    SLIME(165, "slime"),
    BARRIER(166, "barrier"),
    IRON_TRAPDOOR(167, "iron_trapdoor"),
    PRISMARINE(168, "prismarine"),
    SEA_LANTERN(169, "sea_lantern"),
    HAY_BLCOK(170, "hay_block"),
    CARPET(171, "carpet"),
    HARDENED_CAY(172, "hardened_clay"),
    COAL_BLOCK(173, "coal_block"),
    PACKED_ICE(174, "packed_ice"),
    DOUBLE_PLANT(175, "double_plant"),
    STANDING_BANNER(176, "standing_banner"),
    WALL_BANNER(177, "wall_banner"),
    DAYLIGHT_DETECTOR_INVERTED(178, "daylight_detector_inverted"),
    RED_STANDSTONE(179, "red_sandstone"),
    RED_STANDSTONE_STAIRS(180, "red_sandstone_stairs"),
    DOUBLE_STONE_SLAB2(181, "double_stone_slab2"),
   	STONE_SLAB2(182, "stone_slab2"),
    SRUCE_FENCE_GATE(183, "spruce_fence_gate"),
    BIRCH_FENCE_GATE(184, "birch_fence_gate"),
    JUNGLE_FENCE_GATE(185, "jungle_fence_gate"),
    DARK_OAK_FENCE_GATE(186, "dark_oak_fence_gate"),
    ACACIA_FENCE_GATE(187, "acacia_fence_gate"),
    SPRUCE_FENCE(188, "spruce_fence"),
    BIRCH_FENCE(189, "birch_fence"),
    JUNGLE_FENCE(190, "jungle_fence"),
    DARK_OAK_FENCE(191, "dark_oak_fence"),
    ACACIA_FENCE(192, "acacia_fence"),
    SPRUCE_DOOR(193, "spruce_door"),
    BIRCH_DOOR(194, "birch_door"),
    JUNGLE_DOOR(195, "jungle_door"),
    ACACIA_DOOR(196, "acacia_door"),
    DARK_OAK_DOOR(197, "dark_oak_door"),
    END_ROD(198, "end_rod"),
    CHORUS_PLANT(199, "chorus_plant"),
    CHORUS_FLOWER(200, "chorus_flower"),
    PURPUR_BLOCK(201, "purpur_block"),
    PURPUR_PILLAR(202, "purpur_pillar"),
    PURPUR_STAIRS(203, "purpur_stairs"),
    PURPUR_DOUBLE_SLAB(204, "purpur_double_slab"),
    PURPUR_SLAB(205, "purpur_slab"),
    END_BRICKS(206, "end_bricks"),
    BEETROOTS(207, "beetroots"),
    GRASS_PATH(208, "grass_path"),
    END_GATEWAY(209, "end_gateway"),
    REPEATING_COMMAND_BLOCK(210, "repeating_command_block"),
    CHAIN_COMMAND_BLOCK(211, "chain_command_block"),
    FROSTED_ICE(212, "frosted_ice"),
    MAGMA(213, "magma"),
    NETHER_WART_BLOCK(214, "nether_wart_block"),
    RED_NETHER_BRICK(215, "red_nether_brick"),
    BONE_BLOCK(216, "bone_block"),
    STRUCTURE_VOID(217, "structure_void"),
    OBSERVER(218, "observer"),
    WHITE_SHULKER_BOX(219, "white_shulker_box"),
    ORANGE_SHULKER_BOX(220, "orange_shulker_box"),
    MAGENTA_SHULKER_BOX(221, "magenta_shulker_box"),
    LIGHT_BLUE_SHULKER_BOX(222, "light_blue_shulker_box"),
    YELLOW_SHULKER_BOX(223, "yellow_shulker_box"),
    LIME_SHULKER_BOX(224, "lime_shulker_box"),
    PINK_SHULKER_BOX(225, "pink_shulker_box"),
    GRAY_SHULKER_BOX(226, "gray_shulker_box"),
    SILVER_SHULKER_BOX(227, "silver_shulker_box"),
    CYAN_SHULKER_BOX(228, "cyan_shulker_box"),
    PURPLE_SHULKER_BOX(229, "purple_shulker_box"),
    BLUE_SHULKER_BOX(230, "blue_shulker_box"),
    BROWN_SHULKER_BOX(231, "brown_shulker_box"),
    GREEN_SHULKER_BOX(232, "green_shulker_box"),
    RED_SHULKER_BOX(233, "red_shulker_box"),
    BLACK_SHULKER_BOX(234, "black_shulker_box"),
    WHITE_GLAZED_TERRACOTTA(235, "white_glazed_terracotta"),
    ORANGE_GLAZED_TERRACOTTA(236, "orange_glazed_terracotta"),
    MAGENTA_GLAZED_TERRACOTTA(237, "magenta_glazed_terracotta"),
    LIGHT_BLUE_GLAZED_TERRACOTTA(238, "light_blue_glazed_terracotta"),
    YELLOW_GLAZED_TERRACOTTA(239, "yellow_glazed_terracotta"),
    LIME_GLAZED_TERRACOTTA(240, "lime_glazed_terracotta"),
    PINK_GLAZED_TERRACOTTA(241, "pink_glazed_terracotta"),
    GRAY_GLAZED_TERRACOTTA(242, "gray_glazed_terracotta"),
    SILVER_GLAZED_TERRACOTTA(243, "silver_glazed_terracotta"),
    CYAN_GLAZED_TERRACOTTA(244, "cyan_glazed_terracotta"),
    PURPLE_GLAZED_TERRACOTTA(245, "purple_glazed_terracotta"),
    BLUE_GLAZED_TERRACOTTA(246, "blue_glazed_terracotta"),
    BROWN_GLAZED_TERRACOTTA(247, "brown_glazed_terracotta"),
    GREEN_GLAZED_TERRACOTTA(248, "green_glazed_terracotta"),
    RED_GLAZED_TERRACOTTA(249, "red_glazed_terracotta"),
    BLACK_GLAZED_TERRACOTTA(250, "black_glazed_terracotta"),
    CONCRETE(251, "concrete"),
    CONCRETE_POWDER(252, "concrete_powder"),
    STRUCTURE_BLOCK(255, "structure_block");
	
	public final int numericId;
	public final ResourceLocation id;
	
	private Block(int num, String str) {
		this.numericId = num;
		this.id = new ResourceLocation(str);
	}
	
	public IBlockState getDefaultState() {
		return new IBlockState () {

			@Override
			public Collection<IProperty<?>> getPropertyKeys() {
				return Collections.emptyList();
			}

			@Override
			public <T extends Comparable<T>> T getValue(IProperty<T> property) {
				return null; //TODO
			}

			@Override
			public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
				return null; //TODO
			}

			@Override
			public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {
				return null; //TODO
			}

			@Override
			public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
				return null; //TODO
			}

			@Override
			public Block getBlock() {
				return Block.this;
			}
			
		};
	}
	
	public static Block byResourceLocation(ResourceLocation location) {
		for(Block b: values()) if(b.id.equals(location)) return b;
		return null;
	}
    
}
