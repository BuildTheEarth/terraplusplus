package io.github.terra121;

import io.github.terra121.dataset.OpenStreetMaps;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TerraMod.MODID)
public class TerraConfig {
    @Name("overpass_interpreter")
    @Comment({ "Overpass interpreter for road and water OpenStreetMap data",
            "Make sure you follow the instances guidelines",
            "URL must be able to take interpreter input by adding a \"?\"",
            "e.x. \"https://.../api/interpreter\"" })
    public static String serverOverpassDefault = "https://overpass.kumi.systems/api/interpreter"; //"https://overpass-api.de/api/interpreter"

    @Name("fallback_overpass_interpreter")
    @Comment("This is the same as overpass_interpreter, except it's only used as a fallback when overpass_interpreter is down")
    public static String serverOverpassFallback = "https://lz4.overpass-api.de/api/interpreter";

    @Name("overpass_fallback_check_delay")
    @Comment({ "The delay for which to switch to the fallback overpass endpoint",
            "After that time, the game will try switching back to the main one if possible,",
            "This is in minutes" })
    @RangeInt(min = 1)
    public static int overpassCheckDelay = 30;

    @Name("reduced_console_messages")
    @Comment({ "Removes all of Terra121's messages which contain various links in the server console",
            "This is just if it seems to spam the console, it is purely for appearance" })
    public static boolean reducedConsoleMessages;

    @Name("cache_size")
    @Comment({ "Amount of tiles to keep in memory at once",
            "This applies to both tree data and height data",
            "Every tile takes exactly 262,144 bytes of memory (plus some support structures)",
            "The memory requirement for the tiles will be about cacheSize/2 MB",
            "Warning: This number should be at least 4*playerCount to prevent massive slowdowns and internet usage, lower at your own risk" })
    @RangeInt(min = 1)
    @RequiresWorldRestart
    public static int cacheSize = 100;

    @Name("osm_cache_size")
    @Comment({ "Number of OSM regions to keep data about at a time",
            "(these tiles are roughly 1,850 meters/blocks in length but this varies based on position and projection) (they are exactly 1 arcminute across)",
            "Warning: The amount of memory taken by theses tiles fluctuates based on region and is not well studied, raise at your own risk",
            "Warning: This number should be at least 9*playerCount to prevent massive slowdowns and internet useage, lower at your own risk" })
    @RangeInt(min = 1)
    @RequiresMcRestart
    public static int osmCacheSize = 1000;

    @Name("three_water")
    @Comment({ "Require 3 water sources in order to form a new source instead of the vanilla 2",
            "This will make generated streams more stable but will disrupt vanilla water mechanics like 2x2 infinite water sources",
            "Highly expiremental, use at your own risk" })
    public static boolean threeWater;

    @Comment({
            "Configure the servers that terraplusplus will fetch terrain data from."
    })
    public static Data data = new Data();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (TerraMod.MODID.equals(event.getModID())) {
            ConfigManager.sync(TerraMod.MODID, Config.Type.INSTANCE);
            OpenStreetMaps.cancelFallbackThread();
            OpenStreetMaps.setOverpassEndpoint(serverOverpassDefault);
        }
    }

    public static class Data {
        public String[] trees = {
                "https://gis-treecover.wri.org/arcgis/rest/services/TreeCover2000/ImageServer/exportImage?f=image&bbox=${tile.lon.min},${tile.lat.min},${tile.lon.max},${tile.lat.max}&imageSR=4152&bboxSR=4152&format=tiff&adjustAspectRatio=false&&interpolation=RSP_CubicConvolution&size=256,256"
        };

        public String[] heights = {
                "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/${tile.zoom}/${tile.x}/${tile.z}.png"
        };

        @Comment({
                "The number of times to attempt to re-download data tiles in the event of a failure."
        })
        @RangeInt(min = 1)
        public int retryCount = 2;
    }
}
