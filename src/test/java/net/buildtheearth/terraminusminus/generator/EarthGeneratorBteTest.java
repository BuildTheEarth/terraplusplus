package net.buildtheearth.terraminusminus.generator;

import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.substitutes.Biome;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


import static java.lang.Math.*;
import static java.util.concurrent.TimeUnit.*;
import static net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generation with BTE settings")
public class EarthGeneratorBteTest {

    private final EarthGeneratorSettings BTE_SETTINGS = EarthGeneratorSettings.parse(BTE_DEFAULT_SETTINGS);
    private final ChunkDataLoader DATA_LOADER = new ChunkDataLoader(BTE_SETTINGS);
    private final GeographicProjection PROJECTION = BTE_SETTINGS.projection();

    @Test
    @Timeout(value = 5, unit = MINUTES)
    @DisplayName("Elevation accuracy")
    void canGenerateChunksWithAccurateElevation() throws Exception {

        // Oceans and seas
        this.assertSurfaceBetweenInColumn(-3, 3, -36.022625, 54.192081);    // North Atlantic ocean
        this.assertSurfaceBetweenInColumn(-3, 3,  -16.467317, -36.146636);  // South Atlantic ocean
        this.assertSurfaceBetweenInColumn(-3, 3, 31.782243, 32.985489);     // Mediterranean sea between Cyprus and Egypt
        this.assertSurfaceBetweenInColumn(-3, 3, 37.925382, 19.574751);     // Red sea
        this.assertSurfaceBetweenInColumn(-3, 3,30.077690, 42.470698);      // Black Sea
        this.assertSurfaceBetweenInColumn(-3, 3, 87.483183, 15.628939);     // North Indian ocean
        this.assertSurfaceBetweenInColumn(-3, 3, 86.645608, -44.699598);    // South Indian ocean
        this.assertSurfaceBetweenInColumn(-3, 3, 157.484684, 41.933519);    // North Pacific ocean
        this.assertSurfaceBetweenInColumn(-3, 3, 84.665288, -30.308783);    // South Pacific ocean

        // Points from various datasets (no point in testing multiple points from the same dataset)
        this.assertSurfaceBetweenInColumn(35, 45, 24.839508, 59.414132);    // Estonia, Tallinn airport
        this.assertSurfaceBetweenInColumn(275, 285, 14.591481, 46.059114);  // Slovenia
        this.assertSurfaceBetweenInColumn(418, 422, 8.547981, 47.458276);   // Switzerland, Zurich airport
        this.assertSurfaceBetweenInColumn(207, 212, 14.471468, 50.067445);  // Czechia, Prague, Fortuna Arena
        this.assertSurfaceBetweenInColumn(40, 50, 2.414900d, 48.829561d);   // France, Paris

    }

    @Test
    @Timeout(value = 5, unit = MINUTES)
    @DisplayName("Biome correctness test (Terra121 biome filter)")
    void canGenerateChunksWithProperBiomes() throws Exception {
        // Paris, France
        this.assertBiomeAtExactLocation(SWAMPLAND, 2.3547370724689443, 48.8525735128455);
        this.assertBiomeAtExactLocation(RIVER, 2.359191681092456, 48.852107789198286);

        // Monument valley, USA
        this.assertBiomeAtExactLocation(MESA, -112.15281699494163, 36.10248423941173);

        // Agadir, Morocco
        this.assertBiomeAtExactLocation(FOREST, -9.551536999999998, 30.393133000000006);

        // Algeria, middle of the Sahara
        this.assertBiomeAtExactLocation(DESERT, -1.3895949999999941, 26.547157000000006);

        // Brazil, middle of the Amazon
        this.assertBiomeAtExactLocation(JUNGLE, -57.16164554856467, -5.683882501403943);

        // Birchip, Australia
        this.assertBiomeAtExactLocation(PLAINS, 142.64390200000003, -35.964308999999986);

        // Iceland
        this.assertBiomeAtExactLocation(ICE_MOUNTAINS, -16.720414000000005, 64.432562);

        // Indian ocean
        this.assertBiomeAtExactLocation(DEEP_OCEAN, 80.310835, -9.427295);
    }

    private void assertSurfaceBetweenInColumn(int minAltitude, int maxAltitude, double longitude, double latitude) throws Exception {
        CachedChunkData chunk = this.getChunkDataAtGeoPos(longitude, latitude);
        assertSurfaceBetweenInColumn(minAltitude, maxAltitude, chunk);
    }

    private void assertBiomeAtExactLocation(Biome expected, double longitude, double latitude) throws Exception {
        Biome actualBiome = this.getBiomeAtGeoPos(longitude, latitude);
        assertEquals(expected, actualBiome);
    }

    private static void assertSurfaceBetweenInColumn(int minAltitude, int maxAltitude, CachedChunkData chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surfaceHeight = chunk.surfaceHeight(x, z);
                if (surfaceHeight < minAltitude) {
                    throw new AssertionError("Surface height below minimum of " + minAltitude + "m, found spot at " + surfaceHeight + "m.");
                }
                if (surfaceHeight > maxAltitude) {
                    throw new AssertionError("Surface height above maximum of " + maxAltitude + "m, found spot at " + surfaceHeight + "m.");
                }
            }
        }
    }

    private CachedChunkData getChunkDataAtGeoPos(double longitude, double latitude) throws Exception {
        double[] worldPos = PROJECTION.fromGeo(longitude, latitude);
        ChunkPos pos = new ChunkPos(
                (int) round(worldPos[0]) >> 4,
                (int) round(worldPos[1]) >> 4
        );
        return DATA_LOADER.load(pos).get();
    }

    private Biome getBiomeAtGeoPos(double longitude, double latitude) throws Exception {
        double[] worldPos = PROJECTION.fromGeo(longitude, latitude);
        long x = round(worldPos[0]);
        long z = round(worldPos[1]);
        ChunkPos pos = new ChunkPos(
                (int) x >> 4,
                (int) z >> 4
        );
        CachedChunkData data = DATA_LOADER.load(pos).get();
        return data.biome((int) x & 0x0F, (int) z & 0x0F);
    }

    private static final Biome
            PLAINS = Biome.parse("plains"),
            DESERT = Biome.parse("desert"),
            FOREST = Biome.parse("forest"),
            SWAMPLAND = Biome.parse("swampland"),
            RIVER = Biome.parse("river"),
            ICE_MOUNTAINS = Biome.parse("ice_mountains"),
            JUNGLE = Biome.parse("jungle"),
            DEEP_OCEAN = Biome.parse("deep_ocean"),
            MESA = Biome.parse("mesa");

}
