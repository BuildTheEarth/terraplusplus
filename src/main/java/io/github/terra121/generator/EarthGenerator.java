package io.github.terra121.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.event.InitCubicStructureGeneratorEvent;
import io.github.opencubicchunks.cubicchunks.cubicgen.BasicCubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacerProvider;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.OceanWaterReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.TerrainShapeReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicCaveGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicRavineGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature.CubicStrongholdGenerator;
import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.Heights;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.Trees;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.generator.cache.CachedChunkData;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.generator.populate.IEarthPopulator;
import io.github.terra121.generator.populate.TreePopulator;
import io.github.terra121.generator.populate.SnowPopulator;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

public class EarthGenerator extends BasicCubeGenerator {
    public static boolean isNullIsland(int chunkX, int chunkZ) {
        return abs(chunkX) < 5 && abs(chunkZ) < 5;
    }

    public final BiomeProvider biomes;
    public final GeographicProjection projection;
    private final CustomGeneratorSettings cubiccfg;

    public final IBiomeBlockReplacer[][] biomeBlockReplacers;

    private final List<ICubicStructureGenerator> structureGenerators = new ArrayList<>();

    private final List<IEarthPopulator> populators = new ArrayList<>();
    private final Map<Biome, ICubicPopulator> biomePopulators = new IdentityHashMap<>();
    public final EarthGeneratorSettings cfg;

    public final LoadingCache<ChunkPos, CompletableFuture<CachedChunkData>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .softValues()
            .build(new ChunkDataLoader(this));

    //
    // DATASETS
    //

    public final ScalarDataset heights;
    public final OpenStreetMap osm;
    public final ScalarDataset trees;

    public EarthGenerator(World world) {
        super(world);

        this.cfg = new EarthGeneratorSettings(world.getWorldInfo().getGeneratorOptions());
        this.cubiccfg = this.cfg.getCustomCubic();
        this.projection = this.cfg.getProjection();

        boolean doRoads = this.cfg.settings.roads && world.getWorldInfo().isMapFeaturesEnabled();
        boolean doBuildings = this.cfg.settings.buildings && world.getWorldInfo().isMapFeaturesEnabled();

        this.biomes = world.getBiomeProvider(); //TODO: make this not order dependent

        this.osm = new OpenStreetMap(this.projection, doRoads, this.cfg.settings.osmwater, doBuildings);
        this.heights = new Heights(this.cfg.settings.osmwater ? this.osm.water : null, 13, this.cfg.settings.smoothblend ? BlendMode.SMOOTH : BlendMode.LINEAR);
        this.trees = new Trees();

        this.populators.add(TreePopulator.INSTANCE);

        //structures
        if (this.cubiccfg.caves) {
            InitCubicStructureGeneratorEvent caveEvent = new InitCubicStructureGeneratorEvent(InitMapGenEvent.EventType.CAVE, new CubicCaveGenerator());
            MinecraftForge.TERRAIN_GEN_BUS.post(caveEvent);
            this.structureGenerators.add(caveEvent.getNewGen());
        }
        if (this.cubiccfg.ravines) {
            InitCubicStructureGeneratorEvent ravineEvent = new InitCubicStructureGeneratorEvent(InitMapGenEvent.EventType.RAVINE, new CubicRavineGenerator(this.cubiccfg));
            MinecraftForge.TERRAIN_GEN_BUS.post(ravineEvent);
            this.structureGenerators.add(ravineEvent.getNewGen());
        }
        if (this.cubiccfg.strongholds) {
            InitCubicStructureGeneratorEvent strongholdsEvent = new InitCubicStructureGeneratorEvent(InitMapGenEvent.EventType.STRONGHOLD, new CubicStrongholdGenerator(this.cubiccfg));
            MinecraftForge.TERRAIN_GEN_BUS.post(strongholdsEvent);
            this.structureGenerators.add(strongholdsEvent.getNewGen());
        }

        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            this.biomePopulators.put(biome, cubicBiome.getDecorator(this.cubiccfg));
        }

        BiomeBlockReplacerConfig conf = this.cubiccfg.replacerConfig;
        Map<Biome, List<IBiomeBlockReplacer>> biomeBlockReplacers = new IdentityHashMap<>();
        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            Iterable<IBiomeBlockReplacerProvider> providers = cubicBiome.getReplacerProviders();
            List<IBiomeBlockReplacer> replacers = new ArrayList<>();
            for (IBiomeBlockReplacerProvider prov : providers) {
                replacers.add(prov.create(world, cubicBiome, conf));
            }

            //remove these replacers because they're redundant
            replacers.removeIf(replacer -> replacer instanceof TerrainShapeReplacer || replacer instanceof OceanWaterReplacer);

            biomeBlockReplacers.put(biome, replacers);
        }
        this.biomeBlockReplacers = new IBiomeBlockReplacer[biomeBlockReplacers.keySet().stream().mapToInt(Biome::getIdForBiome).max().orElse(0) + 1][];
        biomeBlockReplacers.forEach((biome, replacers) -> this.biomeBlockReplacers[Biome.getIdForBiome(biome)] = replacers.toArray(new IBiomeBlockReplacer[0]));
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) { //legacy compat method
        return this.generateCube(cubeX, cubeY, cubeZ, new CubePrimer());
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer) {
        CachedChunkData data = this.cache.getUnchecked(new ChunkPos(cubeX, cubeZ)).join();

        //build ground surfaces
        this.generateSurface(cubeX, cubeY, cubeZ, primer, data, this.world.getChunk(cubeX, cubeZ).getBiomeArray());

        //add water
        this.generateWater(cubeX, cubeY, cubeZ, primer, data);

        //generate structures
        this.structureGenerators.forEach(gen -> gen.generate(this.world, primer, new CubePos(cubeX, cubeY, cubeZ)));

        if (data.intersectsSurface(cubeY)) { //render complex geometry onto cube surface
            //segments (roads, building outlines, streams, etc.)
            for (Segment s : data.segments()) {
                s.type.fillType().fill(data.heights, primer, s, cubeX, cubeY, cubeZ);
            }
        }

        return primer;
    }

    private void generateSurface(int cubeX, int cubeY, int cubeZ, CubePrimer primer, CachedChunkData data, byte[] biomes) {
        IBlockState stone = Blocks.STONE.getDefaultState();
        if (data.belowSurface(cubeY + 2)) { //below surface -> solid stone (padding of 2 cubes because some replacers might need it)
            //technically, i could reflectively get access to the primer's underlying char[] and use Arrays.fill(), because this
            // implementation causes 4096 calls to ObjectIntIdentityMap#get() when only 1 would be necessary...
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        primer.setBlockState(z, y, z, stone);
                    }
                }
            }
        } else if (data.aboveSurface(cubeY)) { //above surface -> air (no padding here, replacers don't normally affect anything above the surface)
            //no-op, the primer is already air!
        } else {
            double[] heights = data.heights();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double height = heights[x * 16 + z];
                    double dx = x == 15 ? height - heights[(x - 1) * 16 + z] : heights[(x + 1) * 16 + z] - height;
                    double dz = z == 15 ? height - heights[x * 16 + (z - 1)] : heights[x * 16 + (z + 1)] - height;

                    int maxY = min((int) ceil(height) - Coords.cubeToMinBlock(cubeY), 16);
                    if (maxY > 0) {
                        int blockX = Coords.cubeToMinBlock(cubeX) + x;
                        int blockZ = Coords.cubeToMinBlock(cubeZ) + z;
                        IBiomeBlockReplacer[] replacers = this.biomeBlockReplacers[biomes[x * 16 + z] & 0xFF];
                        for (int y = 0; y < maxY; y++) {
                            int blockY = Coords.cubeToMinBlock(cubeY) + y;
                            double density = height - blockY;
                            IBlockState state = stone;
                            for (IBiomeBlockReplacer replacer : replacers) {
                                state = replacer.getReplacedBlock(state, blockX, blockY, blockZ, dx, -1.0d, dz, density);
                            }

                            //calling this explicitly increases the likelihood of JIT inlining it
                            //(for reference: previously, CliffReplacer was manually added to each biome as the last replacer)
                            state = CliffReplacer.INSTANCE.getReplacedBlock(state, blockX, blockY, blockZ, dx, -1.0d, dz, density);

                            primer.setBlockState(x, y, z, state);
                        }
                    }
                }
            }
        }
    }

    private void generateWater(int cubeX, int cubeY, int cubeZ, CubePrimer primer, CachedChunkData data) {
        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState water = Blocks.WATER.getDefaultState();
        if (data.belowSurface(cubeY + 2)) { //below surface -> no water will generate here (padding of 2 cubes because some replacers might need it)
            //no-op, the primer is already solid stone!
        } else if (cubeY < 0) { //y=0 is sea level
            //replace all air blocks with water
            for (int y = 0; y < 16; y++) { //YZX is more cache-friendly, as it's the same coordinate order as CubePrimer uses
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (primer.getBlockState(x, y, z) == air) {
                            primer.setBlockState(x, y, z, water);
                        }
                    }
                }
            }
        } else { //TODO: this works, but is slow and i need to redo it to work with polygons
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double height = data.heights[x * 16 + z];
                    double wateroff = data.wateroffs[x * 16 + z];

                    int minblock = Coords.cubeToMinBlock(cubeY);

                    if (abs(cubeX) < 5 && abs(cubeZ) < 5) {
                        //NULL ISLAND
                    } else if (this.cfg.settings.osmwater) {
                        if (wateroff > 1) {
                            int start = (int) (height);
                            if (start == 0) {
                                start = -1; //elev 0 should still be treated as ocean when in ocean
                            }

                            start -= minblock;
                            if (start < 0) {
                                start = 0;
                            }
                            for (int y = start; y < 16 && y <= -1 - minblock; y++) {
                                primer.setBlockState(x, y, z, water);
                            }
                        } else if (wateroff > 0.4) {
                            int start = (int) (height - (wateroff - 0.4) * 4) - minblock;
                            if (start < 0) {
                                start = 0;
                            }
                            for (int y = start; y < 16 && y < height - minblock; y++) {
                                primer.setBlockState(x, y, z, water);
                            }
                        }
                    } else {
                        for (int y = (int) max(height - minblock, 0); y < 16 && y < -minblock; y++) {
                            primer.setBlockState(x, y, z, water);
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void populate(ICube cube) {
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(this.world, cube))) {
            Random rand = Coords.coordsSeedRandom(this.world.getSeed(), cube.getX(), cube.getY(), cube.getZ());

            CachedChunkData data = this.cache.getUnchecked(cube.getCoords().chunkPos()).join();

            Biome biome = cube.getBiome(Coords.getCubeCenter(cube));

            if (this.cfg.settings.dynamicbaseheight) {
                this.cubiccfg.expectedBaseHeight = (float) data.heights[8 * 16 + 8];
            }

            MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Pre(this.world, rand, cube.getX(), cube.getY(), cube.getZ(), false));

            if (data.intersectsSurface(cube.getY())) {
                for (IEarthPopulator populator : this.populators) {
                    populator.populate(this.world, rand, cube.getCoords(), biome, data);
                }
            }

            this.biomePopulators.get(biome).generate(this.world, rand, cube.getCoords(), biome);

            if (data.aboveSurface(cube.getY())) {
                SnowPopulator.INSTANCE.populate(this.world, rand, cube.getCoords(), biome, data);
            }

            MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Post(this.world, rand, cube.getX(), cube.getY(), cube.getZ(), false));

            //other mod generators
            CubeGeneratorsRegistry.generateWorld(this.world, rand, cube.getCoords(), biome);
        }
    }

    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        // eyes of ender are now compasses
        if ("Stronghold".equals(name)) {
            try {
                double[] vec = this.projection.vector(pos.getX(), pos.getZ(), 1, 0); //direction's to one meter north of here

                //normalize vector
                double mag = Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1]);
                vec[0] /= mag;
                vec[1] /= mag;

                //project vector 100 blocks out to get "stronghold" position
                return new BlockPos((int) (pos.getX() + vec[0] * 100.0), pos.getY(), (int) (pos.getZ() + vec[1] * 100.0));
            } catch (OutOfProjectionBoundsException e) { //out of bounds, we can't really find where north is...
                //simply return center of world
                return BlockPos.ORIGIN;
            }
        }
        return null;
    }
}
