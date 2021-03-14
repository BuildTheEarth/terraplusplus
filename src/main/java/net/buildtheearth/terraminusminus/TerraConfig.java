package net.buildtheearth.terraminusminus;

//TODO Cleanup unnecessary things
public class TerraConfig {

	public static boolean reducedConsoleMessages;
	public static boolean threeWater;

	public static OSMOpts openstreetmap = new OSMOpts();

	public static HttpOpts http = new HttpOpts();

	public static class OSMOpts {
		public String[] servers = {
				"https://cloud.daporkchop.net/gis/osm/0/"
		};
	}

	public static class HttpOpts {

		public String[] maxConcurrentRequests = {
				"8: https://cloud.daporkchop.net/",
				"8: https://s3.amazonaws.com/",
				"1: http://gis-treecover.wri.org/",
				"1: https://overpass.kumi.systems/",
				"1: https://lz4.overpass-api.de/"
		};

		public boolean cache = true;

		public int cacheTTL = 1440;
	}
}
