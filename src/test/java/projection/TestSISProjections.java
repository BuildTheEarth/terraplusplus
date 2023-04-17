package projection;

import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.DymaxionProjection;
import net.buildtheearth.terraplusplus.projection.sis.SISProjectionWrapper;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
import net.minecraft.init.Bootstrap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class TestSISProjections {
    @BeforeClass
    public static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    @SneakyThrows
    public void testTest() {
        //GeographicProjection projection = new SISProjectionWrapper(WKTStandard.WKT2_2015, "PROJCRS[\"OSGB36 / British National Grid\",BASEGEODCRS[\"OSGB36\",DATUM[\"Ordnance Survey of Great Britain 1936\",ELLIPSOID[\"Airy 1830\",6377563.396,299.3249646,LENGTHUNIT[\"metre\",1]]],PRIMEM[\"Greenwich\",0,ANGLEUNIT[\"degree\",0.0174532925199433]]],CONVERSION[\"British National Grid\",METHOD[\"Transverse Mercator\",ID[\"EPSG\",9807]],PARAMETER[\"Latitude of natural origin\",49,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8801]],PARAMETER[\"Longitude of natural origin\",-2,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8802]],PARAMETER[\"Scale factor at natural origin\",0.9996012717,SCALEUNIT[\"unity\",1],ID[\"EPSG\",8805]],PARAMETER[\"False easting\",400000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8806]],PARAMETER[\"False northing\",-100000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8807]]],CS[Cartesian,2],AXIS[\"(E)\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],AXIS[\"(N)\",north,ORDER[2],LENGTHUNIT[\"metre\",1]],SCOPE[\"Engineering survey, topographic mapping.\"],AREA[\"United Kingdom (UK) - offshore to boundary of UKCS within 49°45'N to 61°N and 9°W to 2°E; onshore Great Britain (England, Wales and Scotland). Isle of Man onshore.\"],BBOX[49.75,-9,61.01,2.01],ID[\"EPSG\",27700]]");

        GeographicProjection proj1 = new DymaxionProjection();

        // PROJCRS["WGS 84 / Terra++ Dymaxion", BASEGEODCRS["WGS 84", DATUM["World Geodetic System 1984", ELLIPSOID["WGS 84", 6378137.0, 298.257223563, UNIT["metre", 1]]], PRIMEM["Greenwich", 0.0, UNIT["degree", 0.017453292519943295]]], CONVERSION["Terra++ Dymaxion", METHOD["Terra++ Internal Projection"], PARAMETER["type", "dymaxion"]], CS[Cartesian, 2], AXIS["Easting (X)", east, ORDER[1]], AXIS["Northing (Y)", north, ORDER[2]], UNIT["metre", 1], SCOPE["Minecraft."], AREA["World."], BBOX[-90.00, -180.00, 90.00, 180.00]]

        GeographicProjection proj2 = new SISProjectionWrapper(WKTStandard.WKT2_2015,
                "PROJCRS[\"WGS 84 / Terra++ Dymaxion\",\n"
                + "    BASEGEODCRS[\"WGS 84\",\n"
                + "        DATUM[\"World Geodetic System 1984\",\n"
                + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                + "                LENGTHUNIT[\"metre\",1]]],\n"
                + "        PRIMEM[\"Greenwich\", 0,\n"
                + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                + "        ID[\"EPSG\", 4326]],\n"
                + "    CONVERSION[\"Terra++ Dymaxion\",\n"
                + "        METHOD[\"Terra++ Internal Projection\"],\n"
                + "        PARAMETER[\"type\", \"dymaxion\"]],\n"
                + "    CS[Cartesian, 2],\n"
                + "        AXIS[\"X\", east,\n"
                + "            ORDER[1],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "        AXIS[\"Y\", north,\n"
                + "            ORDER[2],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "    SCOPE[\"Minecraft.\"],\n"
                + "    AREA[\"World.\"],\n"
                + "    BBOX[-90, -180, 90, 180]]");

        proj1 = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();

        proj2 = new ScaleProjectionTransform(new SISProjectionWrapper(WKTStandard.WKT2_2015,
                "PROJCRS[\"WGS 84 / BuildTheEarth Conformal Dymaxion\",\n"
                + "    BASEGEODCRS[\"WGS 84\",\n"
                + "        DATUM[\"World Geodetic System 1984\",\n"
                + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                + "                LENGTHUNIT[\"metre\",1]]],\n"
                + "        PRIMEM[\"Greenwich\", 0,\n"
                + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                + "        ID[\"EPSG\", 4326]],\n"
                + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion\",\n"
                + "        METHOD[\"Terra++ Internal Projection\"],\n"
                + "        PARAMETER[\"type\", \"bte_conformal_dymaxion\"]],\n"
                + "    CS[Cartesian, 2],\n"
                + "        AXIS[\"X\", east,\n"
                + "            ORDER[1],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "        AXIS[\"Y\", south,\n"
                + "            ORDER[2],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "    SCOPE[\"Minecraft.\"],\n"
                + "    AREA[\"World.\"],\n"
                + "    BBOX[-90, -180, 90, 180]]"), 7318261.522857145d, 7318261.522857145d);

        //include the scaling within the projection definition!
        proj2 = new SISProjectionWrapper(WKTStandard.WKT2_2015,
                "PROJCRS[\"WGS 84 / BuildTheEarth Conformal Dymaxion\",\n"
                + "    BASEGEODCRS[\"WGS 84\",\n"
                + "        DATUM[\"World Geodetic System 1984\",\n"
                + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                + "                LENGTHUNIT[\"metre\",1]]],\n"
                + "        PRIMEM[\"Greenwich\", 0,\n"
                + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                + "        ID[\"EPSG\", 4326]],\n"
                + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion\",\n"
                + "        METHOD[\"Terra++ Internal Projection\"],\n"
                + "        PARAMETER[\"type\", \"scale\"],"
                + "        PARAMETER[\"json_args\", \"{\"\"delegate\"\": {\"\"bte_conformal_dymaxion\"\": {}}, \"\"x\"\": 7318261.522857145, \"\"y\"\": 7318261.522857145}\"]],\n"
                + "    CS[Cartesian, 2],\n"
                + "        AXIS[\"X\", east,\n"
                + "            ORDER[1],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "        AXIS[\"Y\", south,\n"
                + "            ORDER[2],\n"
                + "            LENGTHUNIT[\"metre\", 1]],\n"
                + "    SCOPE[\"Minecraft.\"],\n"
                + "    AREA[\"World.\"],\n"
                + "    BBOX[-90, -180, 90, 180]]");

        //unfortunately, this has a fair amount of additional floating-point error (off by <= ~1e-11 degrees). maybe that's acceptable? will have to test more...
        /*proj2 = new SISProjectionWrapper(WKTStandard.WKT2_2015,
                "PROJCRS[\"WGS 84 / BuildTheEarth Conformal Dymaxion\",\n"
                + "    BASEGEODCRS[\"WGS 84\",\n"
                + "        DATUM[\"World Geodetic System 1984\",\n"
                + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                + "                LENGTHUNIT[\"metre\",1]]],\n"
                + "        PRIMEM[\"Greenwich\", 0,\n"
                + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                + "        ID[\"EPSG\", 4326]],\n"
                + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion\",\n"
                + "        METHOD[\"Terra++ Internal Projection\"],\n"
                + "        PARAMETER[\"type\", \"bte_conformal_dymaxion\"]],\n"
                + "    CS[Cartesian, 2],\n"
                + "        AXIS[\"X\", east,\n"
                + "            ORDER[1],\n"
                + "            LENGTHUNIT[\"Minecraft Block\", 1.3664447449393513E-7]],\n"
                + "        AXIS[\"Y\", south,\n"
                + "            ORDER[2],\n"
                + "            LENGTHUNIT[\"Minecraft Block\", 1.3664447449393513E-7]],\n"
                + "    SCOPE[\"Minecraft.\"],\n"
                + "    AREA[\"World.\"],\n"
                + "    BBOX[-90, -180, 90, 180]]");*/

        SplittableRandom r = new SplittableRandom(1337);
        for (int i = 0; i < 10000; i++) {
            double lon = r.nextDouble(-180.0d, 180.0d);
            double lat = r.nextDouble(-90.0d, 90.0d);

            double[] result1 = proj1.fromGeo(lon, lat);
            double[] result2 = proj2.fromGeo(lon, lat);
            checkState(Arrays.equals(result1, result2), i);

            double x = result1[0];
            double y = result1[1];

            result1 = proj1.toGeo(x, y);
            result2 = proj2.toGeo(x, y);
            checkState(Arrays.equals(result1, result2), i);
        }
    }

    private static boolean approxEquals(double[] a, double[] b) {
        //noinspection ArrayEquality
        if (a == b) {
            return true;
        } else if (a == null || b == null || a.length != b.length) {
            return false;
        }

        for (int i = 0, len = a.length; i < len; i++) {
            if (!approxEquals(a[i], b[i])) {
                return false;
            }
        }

        return true;
    }

    private static boolean approxEquals(double a, double b) {
        return Math.abs(a - b) < 1e-12d;
    }
}
