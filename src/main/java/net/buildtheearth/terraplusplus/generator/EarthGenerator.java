package net.buildtheearth.terraplusplus.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldServer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.event.InitCubicStructureGeneratorEvent;
import io.github.opencubicchunks.cubicchunks.core.CubicChunks;
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
import lombok.NonNull;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.TerraMod;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

public class EarthGenerator extends BasicCubeGenerator {
    public static final int WATER_DEPTH_OFFSET = 1;

    static {
        ModContainer cubicchunks = Loader.instance().getIndexedModList().get(CubicChunks.MODID);
        String asyncVersion = "1.12.2-0.0.1175.0"; //the version at which async terrain gen was added
        if (cubicchunks != null && asyncVersion.compareTo(TerraConstants.CC_VERSION) <= 0) {
            //async terrain is supported on this version! register async generation callbacks
            CubeGeneratorsRegistry.registerColumnAsyncLoadingCallback((world, data) -> asyncCallback(world, data.getPos()));
            CubeGeneratorsRegistry.registerCubeAsyncLoadingCallback((world, data) -> asyncCallback(world, data.getPos().chunkPos()));
        } else {
            //we're on an older version of CC that doesn't support async terrain
            TerraMod.LOGGER.error("Async terrain not available!");
            TerraMod.LOGGER.error("Consider updating to the latest version of Cubic Chunks for maximum performance.");

            try {
                MinecraftForge.EVENT_BUS.register(new Object() {
                    @SubscribeEvent
                    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
                        event.player.sendMessage(new TextComponentString(
                                "\u00A7c\u00A7lTerra++ is unable to use async terrain!\n"
                                + "\u00A7c\u00A7lThis will cause significant performance issues.\n"
                                + "\u00A7c\u00A7lUpdate Cubic Chunks to version 1.12.2-0.0.1175.0 or newer to remove this message."
                        ));
                    }
                });
            } catch (Throwable ignored) {
                //this only happens if launching the debug terrain preview without a minecraft context
            }
        }
    }

    private static void asyncCallback(World world, ChunkPos pos) {
        ICubicWorldServer cubicWorld;
        if (world instanceof ICubicWorld && (cubicWorld = (ICubicWorldServer) world).isCubicWorld()) { //ignore vanilla worlds
            ICubeGenerator cubeGenerator = cubicWorld.getCubeGenerator();
            if (cubeGenerator instanceof EarthGenerator) {
                //prefetch terrain data
                try {
                    ((EarthGenerator) cubeGenerator).cache.getUnchecked(pos);
                } catch (Exception e) {
                    //i don't think it's possible any more for an exception to be thrown here, but if one does get thrown somehow this will prevent it
                    //  from screwing up the CC async cube/column loading
                    TerraMod.LOGGER.error("async exception while prefetching data for " + pos, e);
                }
            }
        }
    }

    public static boolean isNullIsland(int chunkX, int chunkZ) {
        return max(chunkX ^ (chunkX >> 31), chunkZ ^ (chunkZ >> 31)) < 3;
    }

    public final EarthGeneratorSettings settings;
    public final BiomeProvider biomes;
    public final GeographicProjection projection;
    private final CustomGeneratorSettings cubiccfg;

    public final IBiomeBlockReplacer[][] biomeBlockReplacers;

    private final List<ICubicStructureGenerator> structureGenerators = new ArrayList<>();

    private final IEarthPopulator[] populators;

    public final GeneratorDatasets datasets;

    public final LoadingCache<ChunkPos, CompletableFuture<CachedChunkData>> cache;

    public EarthGenerator(World world) {
        super(world);

        this.settings = EarthGeneratorSettings.parse(world.getWorldInfo().getGeneratorOptions());
        this.cubiccfg = this.settings.customCubic();
        this.projection = this.settings.projection();

        this.biomes = world.getBiomeProvider();

        this.datasets = this.settings.datasets();
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(5L, TimeUnit.MINUTES)
                .softValues()
                .build(new ChunkDataLoader(this.settings));

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

        this.populators = EarthGeneratorPipelines.populators(this.settings);

        BiomeBlockReplacerConfig conf = this.cubiccfg.createBiomeBlockReplacerConfig();
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
    public boolean supportsConcurrentColumnGeneration() {
        return true;
    }

    @Override
    public GeneratorReadyState pollAsyncColumnGenerator(int chunkX, int chunkZ) {
        CompletableFuture<CachedChunkData> future = this.cache.getUnchecked(new ChunkPos(chunkX, chunkZ));
        if (!future.isDone()) {
            return GeneratorReadyState.WAITING;
        } else if (future.isCompletedExceptionally()) {
            return GeneratorReadyState.FAIL;
        } else {
            return GeneratorReadyState.READY;
        }
    }

    @Override
    public void generateColumn(Chunk column) { //legacy compat method
        CachedChunkData data = this.cache.getUnchecked(column.getPos()).join();
        this.generateColumn(column, data);
    }

    @Override
    public Optional<Chunk> tryGenerateColumn(World world, int columnX, int columnZ, ChunkPrimer primer, boolean forceGenerate) {
        CompletableFuture<CachedChunkData> future = this.cache.getUnchecked(new ChunkPos(columnX, columnZ));
        if (!forceGenerate && (!future.isDone() || future.isCompletedExceptionally())) {
            return Optional.empty();
        }

        Chunk column = new Chunk(world, columnX, columnZ);
        this.generateColumn(column, future.join());
        return Optional.of(column);
    }

    protected void generateColumn(Chunk column, CachedChunkData data) {
        column.setBiomeArray(data.biomes());
    }

    @Override
    public boolean supportsConcurrentCubeGeneration() {
        return true;
    }

    @Deprecated
	@Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) { //legacy compat method
        CubePrimer primer = new CubePrimer();
        CachedChunkData data = this.cache.getUnchecked(new ChunkPos(cubeX, cubeZ)).join();
        this.generateCube(cubeX, cubeY, cubeZ, primer, data);
        return primer;
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer) { //legacy compat method
        CachedChunkData data = this.cache.getUnchecked(new ChunkPos(cubeX, cubeZ)).join();
        this.generateCube(cubeX, cubeY, cubeZ, primer, data);
        return primer;
    }

    @Override
    public GeneratorReadyState pollAsyncCubeGenerator(int cubeX, int cubeY, int cubeZ) {
        return this.pollAsyncColumnGenerator(cubeX, cubeZ);
    }

    @Override
    public Optional<CubePrimer> tryGenerateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer, boolean forceGenerate) {
        CompletableFuture<CachedChunkData> future = this.cache.getUnchecked(new ChunkPos(cubeX, cubeZ));
        if (!forceGenerate && (!future.isDone() || future.isCompletedExceptionally())) {
            return Optional.empty();
        }

        this.generateCube(cubeX, cubeY, cubeZ, primer, future.join());
        return Optional.of(primer);
    }

    protected void generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer, CachedChunkData data) {
        //build ground surfaces
        this.generateSurface(cubeX, cubeY, cubeZ, primer, data);

        //generate structures
        this.structureGenerators.forEach(gen -> gen.generate(this.world, primer, new CubePos(cubeX, cubeY, cubeZ)));

        if (data.intersectsSurface(cubeY)) { //render surface blocks onto cube surface
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = data.surfaceHeight(x, z) - Coords.cubeToMinBlock(cubeY);
                    IBlockState state;
                    if ((y & 0xF) == y //don't set surface blocks outside of this cube
                        && (state = data.surfaceBlock(x, z)) != null) {
                        primer.setBlockState(x, y, z, state);
                    }
                }
            }
        }
    }

    protected void generateSurface(int cubeX, int cubeY, int cubeZ, CubePrimer primer, CachedChunkData data) {
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState water = Blocks.WATER.getDefaultState();
        if (data.belowSurface(cubeY + 2)) { //below surface -> solid stone (padding of 2 cubes because some replacers might need it)
            //technically, i could reflectively get access to the primer's underlying char[] and use Arrays.fill(), because this
            // implementation causes 4096 calls to ObjectIntIdentityMap#get() when only 1 would be necessary...
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        primer.setBlockState(x, y, z, stone);
                    }
                }
            }
        } else if (data.aboveSurface(cubeY)) { //above surface -> air (no padding here, replacers don't normally affect anything above the surface)
        } else {
            IBlockState grass = Blocks.GRASS.getDefaultState();
            IBlockState dirt = Blocks.DIRT.getDefaultState();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int groundHeight = data.groundHeight(x, z);
                    int waterHeight = data.waterHeight(x, z);

                    //horizontal density change is calculated using the top height rather than the ground height
                    int topHeight = data.surfaceHeight(x, z);
                    double dx = x == 15 ? topHeight - data.surfaceHeight(x - 1, z) : data.surfaceHeight(x + 1, z) - topHeight;
                    double dz = z == 15 ? topHeight - data.surfaceHeight(x, z - 1) : data.surfaceHeight(x, z + 1) - topHeight;

                    int groundTop = groundHeight - Coords.cubeToMinBlock(cubeY);
                    int groundTopInCube = min(groundTop, 15);
                    int waterTop = min(waterHeight - Coords.cubeToMinBlock(cubeY), 15);

                    int blockX = Coords.cubeToMinBlock(cubeX) + x;
                    int blockZ = Coords.cubeToMinBlock(cubeZ) + z;
                    IBiomeBlockReplacer[] replacers = this.biomeBlockReplacers[data.biome(x, z) & 0xFF];
                    for (int y = 0; y <= groundTopInCube; y++) {
                        int blockY = Coords.cubeToMinBlock(cubeY) + y;
                        double density = groundTop - y;
                        IBlockState state = stone;
                        for (IBiomeBlockReplacer replacer : replacers) {
                            state = replacer.getReplacedBlock(state, blockX, blockY, blockZ, dx, -1.0d, dz, density);
                        }

                        //calling this explicitly increases the likelihood of JIT inlining it
                        //(for reference: previously, CliffReplacer was manually added to each biome as the last replacer)
                        state = CliffReplacer.INSTANCE.getReplacedBlock(state, blockX, blockY, blockZ, dx, -1.0d, dz, density);

                        if (groundHeight < waterHeight && state == grass) { //hacky workaround for underwater grass
                            state = dirt;
                        }

                        primer.setBlockState(x, y, z, state);
                    }

                    //fill water
                    for (int y = max(groundTopInCube + 1, 0); y <= waterTop; y++) {
                        primer.setBlockState(x, y, z, water);
                    }
                }
            }
        }
    }

    @Override
    public GeneratorReadyState pollAsyncCubePopulator(int cubeX, int cubeY, int cubeZ) {
        //ensure that all columns required for population are ready to go
        // checking all neighbors here improves performance when checking if a cube can be generated
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                CompletableFuture<CachedChunkData> future = this.cache.getUnchecked(new ChunkPos(cubeX + dx, cubeZ + dz));
                if (!future.isDone()) {
                    return GeneratorReadyState.WAITING;
                } else if (future.isCompletedExceptionally()) {
                    return GeneratorReadyState.FAIL;
                }
            }
        }
        return GeneratorReadyState.READY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void populate(ICube cube) {
        if (MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(this.world, cube))) {
            return; //population event was cancelled
        }

        CachedChunkData[] datas = new CachedChunkData[2 * 2];
        for (int i = 0, dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                datas[i++] = this.cache.getUnchecked(new ChunkPos(cube.getX() + dx, cube.getZ() + dz)).join();
            }
        }

        Random random = Coords.coordsSeedRandom(this.world.getSeed(), cube.getX(), cube.getY(), cube.getZ());
        Biome biome = cube.getBiome(Coords.getCubeCenter(cube));

        this.cubiccfg.expectedBaseHeight = (float) datas[0].groundHeight(15, 15);

        for (IEarthPopulator populator : this.populators) {
            populator.populate(this.world, random, cube.getCoords(), biome, datas);
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

    /**
     * {@link CacheLoader} implementation for {@link EarthGenerator} which asynchronously aggregates information from multiple datasets and stores it
     * in a {@link CachedChunkData} for use by the generator.
     *
     * @author DaPorkchop_
     */
    public static class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<CachedChunkData>> {
        protected final GeneratorDatasets datasets;
        protected final IEarthDataBaker<?>[] bakers;

        public ChunkDataLoader(@NonNull EarthGeneratorSettings settings) {
            this.datasets = settings.datasets();
            this.bakers = EarthGeneratorPipelines.dataBakers(settings);
        }

        @Override
        public CompletableFuture<CachedChunkData> load(@NonNull ChunkPos pos) {
            return IEarthAsyncPipelineStep.getFuture(pos, this.datasets, this.bakers, CachedChunkData::builder);
        }
    }
}
