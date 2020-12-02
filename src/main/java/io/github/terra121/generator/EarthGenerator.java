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
import io.github.opencubicchunks.cubicchunks.cubicgen.BasicCubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacerProvider;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.CubicCaveGenerator;
import io.github.terra121.dataset.Heights;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.generator.cache.CachedChunkData;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.populator.CliffReplacer;
import io.github.terra121.populator.EarthTreePopulator;
import io.github.terra121.populator.SnowPopulator;
import io.github.terra121.projection.GeographicProjection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

public class EarthGenerator extends BasicCubeGenerator {
    public final ScalarDataset heights;
    public final OpenStreetMap osm;
    public final Map<Biome, List<IBiomeBlockReplacer>> biomeBlockReplacers = new IdentityHashMap<>();
    public final BiomeProvider biomes;
    public final GeographicProjection projection;
    public final Set<Block> unnaturals = Collections.newSetFromMap(new IdentityHashMap<>());
    private final CustomGeneratorSettings cubiccfg;
    private final Set<ICubicPopulator> surfacePopulators;
    private final Map<Biome, ICubicPopulator> biomePopulators = new IdentityHashMap<>();
    private final CubicCaveGenerator caveGenerator;
    private final SnowPopulator snow;
    public final EarthGeneratorSettings cfg;
    private final boolean doRoads;
    private final boolean doBuildings;

    public final LoadingCache<ChunkPos, CachedChunkData> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .softValues()
            .build(new ChunkDataLoader(this));

    public EarthGenerator(World world) {
        super(world);

        this.cfg = new EarthGeneratorSettings(world.getWorldInfo().getGeneratorOptions());
        this.projection = this.cfg.getProjection();

        this.doRoads = this.cfg.settings.roads && world.getWorldInfo().isMapFeaturesEnabled();
        this.doBuildings = this.cfg.settings.buildings && world.getWorldInfo().isMapFeaturesEnabled();

        this.biomes = world.getBiomeProvider(); //TODO: make this not order dependent

        this.osm = new OpenStreetMap(this.projection, this.doRoads, this.cfg.settings.osmwater, this.doBuildings);
        this.heights = new Heights(this.cfg.settings.osmwater ? this.osm.water : null, 13, this.cfg.settings.smoothblend);

        this.unnaturals.add(Blocks.STONEBRICK);
        this.unnaturals.add(Blocks.CONCRETE);
        this.unnaturals.add(Blocks.BRICK_BLOCK);

        this.surfacePopulators = new HashSet<>();

        this.surfacePopulators.add(new EarthTreePopulator(this.projection));
        this.snow = new SnowPopulator(); //this will go after the rest

        this.cubiccfg = this.cfg.getCustomCubic();

        this.caveGenerator = new CubicCaveGenerator();

        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            this.biomePopulators.put(biome, cubicBiome.getDecorator(this.cubiccfg));
        }

        BiomeBlockReplacerConfig conf = this.cubiccfg.replacerConfig;
        CliffReplacer cliffs = new CliffReplacer();

        for (Biome biome : ForgeRegistries.BIOMES) {
            CubicBiome cubicBiome = CubicBiome.getCubic(biome);
            Iterable<IBiomeBlockReplacerProvider> providers = cubicBiome.getReplacerProviders();
            List<IBiomeBlockReplacer> replacers = new ArrayList<>();
            for (IBiomeBlockReplacerProvider prov : providers) {
                replacers.add(prov.create(world, cubicBiome, conf));
            }
            replacers.add(cliffs);

            this.biomeBlockReplacers.put(biome, replacers);
        }
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) { //legacy compat method
        return this.generateCube(cubeX, cubeY, cubeZ, new CubePrimer());
    }

    //TODO: more efficient
    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        CachedChunkData data = this.cache.getUnchecked(new ChunkPos(cubeX, cubeZ));

        //fill in the world
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double height = data.heights[x * 16 + z];
                double wateroff = data.wateroffs[x * 16 + z];

                //estimate slopes
                double dx;
                double dz;
                if (x == 16 - 1) {
                    dx = data.heights[x * 16 + z] - data.heights[(x - 1) * 16 + z];
                } else {
                    dx = data.heights[(x + 1) * 16 + z] - data.heights[x * 16 + z];
                }

                if (z == 16 - 1) {
                    dz = data.heights[x * 16 + z] - data.heights[x * 16 + z - 1];
                } else {
                    dz = data.heights[x * 16 + z + 1] - data.heights[x * 16 + z];
                }

                //get biome (thanks to 	z3nth10n for spoting this one)
                List<IBiomeBlockReplacer> reps = this.biomeBlockReplacers.get(this.biomes.getBiome(pos.setPos(cubeX * 16 + x, 0, cubeZ * 16 + z)));

                for (int y = 0; y < 16 && y < height - Coords.cubeToMinBlock(cubeY); y++) {
                    IBlockState block = Blocks.STONE.getDefaultState();
                    for (IBiomeBlockReplacer rep : reps) {
                        block = rep.getReplacedBlock(block, cubeX * 16 + x, cubeY * 16 + y + 63, cubeZ * 16 + z, dx, -1, dz, height - (cubeY * 16 + y));
                    }
                    primer.setBlockState(x, y, z, block);
                }

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
                            primer.setBlockState(x, y, z, Blocks.WATER.getDefaultState());
                        }
                    } else if (wateroff > 0.4) {
                        int start = (int) (height - (wateroff - 0.4) * 4) - minblock;
                        if (start < 0) {
                            start = 0;
                        }
                        for (int y = start; y < 16 && y < height - minblock; y++) {
                            primer.setBlockState(x, y, z, Blocks.WATER.getDefaultState());
                        }
                    }
                } else {
                    for (int y = (int) max(height - minblock, 0); y < 16 && y < -minblock; y++) {
                        primer.setBlockState(x, y, z, Blocks.WATER.getDefaultState());
                    }
                }
            }
        }

        if (this.cfg.settings.caves) {
            this.caveGenerator.generate(this.world, primer, new CubePos(cubeX, cubeY, cubeZ));
        }

        if (cubeY >= data.surfaceMinCube && cubeY <= data.surfaceMaxCube) { //spawn roads
            Set<Segment> segments = this.osm.segmentsForChunk(cubeX, cubeZ, 8.0d);
            if (segments != null) {
                segments.stream()
                        .sorted(Comparator.<Segment>comparingInt(s -> s.layer_number).thenComparing(s -> s.type))
                        .forEach(s -> s.type.fillType().fill(data.heights, primer, s, cubeX, cubeY, cubeZ));
            }
        }

        return primer;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void populate(ICube cube) {
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(this.world, cube))) {
            Random rand = Coords.coordsSeedRandom(this.world.getSeed(), cube.getX(), cube.getY(), cube.getZ());

            Biome biome = cube.getBiome(Coords.getCubeCenter(cube));

            if (this.cfg.settings.dynamicbaseheight) {
                double[] proj = this.projection.toGeo((cube.getX() * 16 + 8), (cube.getZ() * 16 + 8));
                this.cubiccfg.expectedBaseHeight = (float) this.heights.estimateLocal(proj[0], proj[1]);
            }

            MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Pre(this.world, rand, cube.getX(), cube.getY(), cube.getZ(), false));

            CubePos pos = cube.getCoords();

            int surf = this.isSurface(this.world, cube);
            if (surf == 0) {
                for (ICubicPopulator pop : this.surfacePopulators) {
                    pop.generate(this.world, rand, pos, biome);
                }
            }

            this.biomePopulators.get(biome).generate(this.world, rand, pos, biome);

            if (surf == 1) {
                this.snow.generate(this.world, rand, pos, biome);
            }

            MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Post(this.world, rand, cube.getX(), cube.getY(), cube.getZ(), false));
            CubeGeneratorsRegistry.generateWorld(this.world, rand, pos, biome);
        }
    }

    //TODO: so inefficient but it's the best i could think of, short of caching this state by coords
    //TODO: factor in if air right above solid cube
    private int isSurface(World world, ICube cube) {
        IBlockState defState = Blocks.AIR.getDefaultState();
        IBlockState type = null;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                type = world.getBlockState(new BlockPos(x + cube.getX() * 16, 16 + cube.getY() * 16, z + cube.getZ() * 16));
                if (type == defState &&
                    cube.getBlockState(x, 0, z) != defState && !this.unnaturals.contains(cube.getBlockState(x, 0, z).getBlock())) {
                    return 0;
                }
            }
        }
        return type == defState ? 1 : -1;
    }

    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        // eyes of ender are now compasses
        if ("Stronghold".equals(name)) {
            double[] vec = this.projection.vector(pos.getX(), pos.getZ(), 1, 0); //direction's to one meter north of here

            //normalize vector
            double mag = Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1]);
            vec[0] /= mag;
            vec[1] /= mag;

            //project vector 100 blocks out to get "stronghold" position
            return new BlockPos((int) (pos.getX() + vec[0] * 100.0), pos.getY(), (int) (pos.getZ() + vec[1] * 100.0));
        }
        return null;
    }
}
