package net.buildtheearth.terraminusminus.generator;

import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


import static java.lang.Math.*;
import static java.util.concurrent.TimeUnit.*;
import static net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings.*;

public class EarthGeneratorElevationTest {

    private final EarthGeneratorSettings BTE_SETTINGS = EarthGeneratorSettings.parse(BTE_DEFAULT_SETTINGS);

    @Test
    @Timeout(value = 5, unit = MINUTES)
    void canGenerateChunksWithAccurateElevation() throws Exception {

        // Oceans and seas
        this.assertSurfaceBetween(-3, 3, -36.022625, 54.192081);    // North Atlantic ocean
        this.assertSurfaceBetween(-3, 3,  -16.467317, -36.146636);  // South Atlantic ocean
        this.assertSurfaceBetween(-3, 3, 31.782243, 32.985489);     // Mediterranean sea between Cyprus and Egypt
        this.assertSurfaceBetween(-3, 3, 37.925382, 19.574751);     // Red sea
        this.assertSurfaceBetween(-3, 3,30.077690, 42.470698);      // Black Sea
        this.assertSurfaceBetween(-3, 3, 87.483183, 15.628939);     // North Indian ocean
        this.assertSurfaceBetween(-3, 3, 86.645608, -44.699598);    // South Indian ocean
        this.assertSurfaceBetween(-3, 3, 157.484684, 41.933519);    // North Pacific ocean
        this.assertSurfaceBetween(-3, 3, 84.665288, -30.308783);    // South Pacific ocean

        // Points from various datasets (no point in testing multiple points from the same dataset)
        this.assertSurfaceBetween(35, 45, 24.839508, 59.414132);    // Estonia, Tallinn airport
        this.assertSurfaceBetween(275, 285, 14.591481, 46.059114);  // Slovenia
        this.assertSurfaceBetween(418, 422, 8.547981, 47.458276);   // Switzerland, Zurich airport
        this.assertSurfaceBetween(207, 212, 14.471468, 50.067445);  // Czechia, Prague, Fortuna Arena
        this.assertSurfaceBetween(40, 50, 2.414900d, 48.829561d);   // France, Paris

    }

    private void assertSurfaceBetween(int minAltitude, int maxAltitude, double longitude, double latitude) throws Exception {
        CachedChunkData chunk = this.getChunkDataAtGeoPos(longitude, latitude);
        assertSurfaceBetween(minAltitude, maxAltitude, chunk);
    }

    private static void assertSurfaceBetween(int minAltitude, int maxAltitude, CachedChunkData chunk) {
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
        ChunkDataLoader loader = new ChunkDataLoader(BTE_SETTINGS);
        GeographicProjection projection = BTE_SETTINGS.projection();
        double[] worldPos = projection.fromGeo(longitude, latitude);
        ChunkPos pos = new ChunkPos(
                (int) round(worldPos[0]) >> 4,
                (int) round(worldPos[1]) >> 4
        );
        return loader.load(pos).get();
    }

}
