package net.buildtheearth.terraplusplus;

import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.util.http.Http;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerraConstants.MODID)
@Config(modid = TerraConstants.MODID)
public class TerraConfig {
    @Name("reduced_console_messages")
    @Comment({ "Removes all of TerraPlusPlus' messages which contain various links in the server console",
            "This is just if it seems to spam the console, it is purely for appearance" })
    public static boolean reducedConsoleMessages;

    @Name("three_water")
    @Comment({ "Require 3 water sources in order to form a new source instead of the vanilla 2",
            "This will make generated streams more stable but will disrupt vanilla water mechanics like 2x2 infinite water sources",
            "Highly expiremental, use at your own risk" })
    @Config.RequiresMcRestart
    public static boolean threeWater;

    @Comment({
            "Configure how terraplusplus will retrieve surface elevation data."
    })
    public static ElevationOpts elevation = new ElevationOpts();

    @Comment({
            "Configure how terraplusplus will retrieve OpenStreetMap data."
    })
    public static OSMOpts openstreetmap = new OSMOpts();

    @Comment({
            "Configure the terraplusplus HTTP client."
    })
    public static HttpOpts http = new HttpOpts();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (TerraConstants.MODID.equals(event.getModID())) {
            ConfigManager.sync(TerraConstants.MODID, Config.Type.INSTANCE);
            Http.configChanged();
        }
    }

    public static class ElevationOpts {
        public String[] servers = {
                "http://10.0.0.20/public/gis/dem/earth/"
        };
    }

    public static class OSMOpts {
        public String[] servers = {
                "https://cloud.daporkchop.net/gis/osm/0/"
        };
    }

    public static class HttpOpts {
        @Comment({
                "Configures the maximum permitted number of concurrent HTTP requests to each of the given hosts.",
                "Each line is an entry, given in the following format:",
                "  \"<number>: <host>\"",
                "Example: \"3: https://example.com/\" will permit up to 3 requests to URLs starting with \"https://example.com/\" to be made at once.",
                "",
                "You are strongly advised not to modify the default settings. Many of these services do not have the capacity to deal with thousands"
                + " of concurrent requests, and raising the limits will only make them slower for everyone while not actually providing any noticeable performance"
                + " improvements for you."
        })
        public String[] maxConcurrentRequests = {
                "8: https://cloud.daporkchop.net/",
                "8: https://s3.amazonaws.com/"
        };

        @Comment({
                "Whether or not to use the persistent data cache.",
                "This is strongly recommended for performance. Disable only for debugging, or if you have EXTREMELY limited storage."
        })
        public boolean cache = true;

        @Comment({
                "The maximum age of data in the persistent cache, in minutes. Data older than this will be expired.",
                "Default: 1440 minutes (1 day)"
        })
        public int cacheTTL = 1440;
    }
}
