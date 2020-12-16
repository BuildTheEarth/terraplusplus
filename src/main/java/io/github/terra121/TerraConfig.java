package io.github.terra121;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TerraMod.MODID)
public class TerraConfig {
    @Name("reduced_console_messages")
    @Comment({ "Removes all of Terra121's messages which contain various links in the server console",
            "This is just if it seems to spam the console, it is purely for appearance" })
    public static boolean reducedConsoleMessages;

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
        }
    }

    public static class Data {
        public String[] trees = {
                "https://cloud.daporkchop.net/gis/treecover2000/${x}/${z}.tiff",
                "http://gis-treecover.wri.org/arcgis/rest/services/TreeCover2000/ImageServer/exportImage?f=image&bbox=${lon.min},${lat.min},${lon.max},${lat.max}&imageSR=4152&bboxSR=4152&format=tiff&adjustAspectRatio=false&&interpolation=RSP_CubicConvolution&size=256,256"
        };

        public String[] heights = {
                "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/${zoom}/${x}/${z}.png"
        };

        public String[] overpass = {
                "https://overpass.kumi.systems/api/interpreter/?data=[out:json];way(${lat.min},${lon.min},${lat.max},${lon.max});out%20geom(${lat.min},${lon.min},${lat.max},${lon.max})%20tags%20qt;(._<;);out%20body%20qt;is_in(${lat.min},${lon.min});area._[~\"natural|waterway\"~\"water|riverbank\"];out%20ids;",
                "https://lz4.overpass-api.de/api/interpreter/?data=[out:json];way(${lat.min},${lon.min},${lat.max},${lon.max});out%20geom(${lat.min},${lon.min},${lat.max},${lon.max})%20tags%20qt;(._<;);out%20body%20qt;is_in(${lat.min},${lon.min});area._[~\"natural|waterway\"~\"water|riverbank\"];out%20ids;"
        };

        @Comment({
                "Whether or not to use the persistent data cache.",
                "This is strongly recommended for performance. Disable only for debugging, or if you have EXTREMELY limited storage."
        })
        public boolean cache = true;

        @Comment({
                "The maximum age of data in the persistent cache, in minutes. Data older than this will be expired.",
                "Default: 2880 minutes (2 days)"
        })
        public int cacheTTL = 2880;
    }
}
