package projection;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.EquirectangularProjection;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.SinusoidalProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.DymaxionProjection;
import net.buildtheearth.terraplusplus.projection.epsg.EPSG3785;
import net.buildtheearth.terraplusplus.projection.epsg.EPSG4326;
import net.buildtheearth.terraplusplus.projection.mercator.CenteredMercatorProjection;
import net.buildtheearth.terraplusplus.projection.mercator.TransverseMercatorProjection;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terraplusplus.projection.sis.SISProjectionWrapper;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import net.buildtheearth.terraplusplus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.SwapAxesProjectionTransform;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.minecraft.init.Bootstrap;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.SplittableRandom;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
public class TestSISProjections {
    private static final double DEFAULT_D = 1e-14d;

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.register();
    }

    protected static void testProjectionAccuracy(@NonNull GeographicProjection proj1, @NonNull GeographicProjection proj2) {
        testProjectionAccuracy(proj1, proj2, DEFAULT_D);
        testProjectionAccuracy(proj1, new SISProjectionWrapper(proj2.projectedCRS()), DEFAULT_D);
    }

    @SneakyThrows(OutOfProjectionBoundsException.class)
    protected static void testProjectionAccuracy(@NonNull GeographicProjection proj1, @NonNull GeographicProjection proj2, double d) {
        SplittableRandom r = new SplittableRandom(1337);
        for (int i = 0; i < 10000; i++) {
            double lon = r.nextDouble(-180.0d, 180.0d);
            double lat = r.nextDouble(-90.0d, 90.0d);

            switch (i) {
                case 0:
                    lon = -180.0d;
                    lat = 0.0d;
                    break;
                case 1:
                    lon = 180.0d;
                    lat = 0.0d;
                    break;
                case 2:
                    lon = 0.0d;
                    lat = -90.0d;
                    break;
                case 3:
                    lon = 0.0d;
                    lat = 90.0d;
                    break;
            }

            double[] result1;
            try {
                result1 = proj1.fromGeo(lon, lat);
            } catch (OutOfProjectionBoundsException e) {
                try {
                    double[] result2 = proj2.fromGeo(lon, lat);
                    //TODO: throw new AssertionError("proj1 threw " + e + ", but proj2 returned " + Arrays.toString(result2) + "?!?");
                    continue;
                } catch (OutOfProjectionBoundsException e1) {
                    //both projections failed with an exception, which is correct
                    continue;
                }
            }
            double[] result2 = proj2.fromGeo(lon, lat);

            assert approxEquals(result1, result2, d)
                    : "fromGeo #" + i + " (" + lat + "°N, " + lon + "°E): " + Arrays.toString(result1) + " != " + Arrays.toString(result2);

            double x = result1[0];
            double y = result1[1];

            result1 = proj1.toGeo(x, y);
            result2 = proj2.toGeo(x, y);
            assert approxEquals(result1, result2, d)
                    : "toGeo #" + i + " (" + lat + "°N, " + lon + "°E) -> (" + x + ", " + y + "): " + Arrays.toString(result1) + " != " + Arrays.toString(result2);

            Matrix2 deriv1 = proj1.fromGeoDerivative(lon, lat);
            Matrix2 deriv2 = proj2.fromGeoDerivative(lon, lat);
            assert veryApproximateEquals(deriv1, deriv2, 0.01d)
                    : "fromGeoDerivative #" + i + " (" + lat + "°N, " + lon + "°E):\n" + deriv1 + "!=\n" + deriv2;

            deriv1 = proj1.toGeoDerivative(x, y);
            deriv2 = proj2.toGeoDerivative(x, y);
            assert veryApproximateEquals(deriv1, deriv2, 0.01d)
                    : "toGeoDerivative #" + i + " (" + lat + "°N, " + lon + "°E) -> (" + x + ", " + y + "):\n" + deriv1 + "!=\n" + deriv2;
        }
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testDymaxion() {
        //GeographicProjection projection = new SISProjectionWrapper(WKTStandard.WKT2_2015, "PROJCRS[\"OSGB36 / British National Grid\",BASEGEODCRS[\"OSGB36\",DATUM[\"Ordnance Survey of Great Britain 1936\",ELLIPSOID[\"Airy 1830\",6377563.396,299.3249646,LENGTHUNIT[\"metre\",1]]],PRIMEM[\"Greenwich\",0,ANGLEUNIT[\"degree\",0.0174532925199433]]],CONVERSION[\"British National Grid\",METHOD[\"Transverse Mercator\",ID[\"EPSG\",9807]],PARAMETER[\"Latitude of natural origin\",49,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8801]],PARAMETER[\"Longitude of natural origin\",-2,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8802]],PARAMETER[\"Scale factor at natural origin\",0.9996012717,SCALEUNIT[\"unity\",1],ID[\"EPSG\",8805]],PARAMETER[\"False easting\",400000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8806]],PARAMETER[\"False northing\",-100000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8807]]],CS[Cartesian,2],AXIS[\"(E)\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],AXIS[\"(N)\",north,ORDER[2],LENGTHUNIT[\"metre\",1]],SCOPE[\"Engineering survey, topographic mapping.\"],AREA[\"United Kingdom (UK) - offshore to boundary of UKCS within 49°45'N to 61°N and 9°W to 2°E; onshore Great Britain (England, Wales and Scotland). Isle of Man onshore.\"],BBOX[49.75,-9,61.01,2.01],ID[\"EPSG\",27700]]");

        // PROJCRS["WGS 84 / Terra++ Dymaxion", BASEGEODCRS["WGS 84", DATUM["World Geodetic System 1984", ELLIPSOID["WGS 84", 6378137.0, 298.257223563, UNIT["metre", 1]]], PRIMEM["Greenwich", 0.0, UNIT["degree", 0.017453292519943295]]], CONVERSION["Terra++ Dymaxion", METHOD["Terra++ Internal Projection"], PARAMETER["type", "dymaxion"]], CS[Cartesian, 2], AXIS["Easting (X)", east, ORDER[1]], AXIS["Northing (Y)", north, ORDER[2]], UNIT["metre", 1], SCOPE["Minecraft."], AREA["World."], BBOX[-90.00, -180.00, 90.00, 180.00]]

        testProjectionAccuracy(
                new DymaxionProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / Terra++ Dymaxion\",\n"
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
                        + "    BBOX[-90, -180, 90, 180]]"));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testBTE1() {
        testProjectionAccuracy(
                EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection(),
                new ScaleProjectionTransform(new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / BuildTheEarth Conformal Dymaxion (Unscaled)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\", 0,\n"
                        + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                        + "        ID[\"EPSG\", 4326]],\n"
                        + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion (Unscaled)\",\n"
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
                        + "    BBOX[-90, -180, 90, 180]]"), 7318261.522857145d, 7318261.522857145d));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testBTE2() {
        testProjectionAccuracy(
                EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / BuildTheEarth Conformal Dymaxion (Scaled)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\", 0,\n"
                        + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                        + "        ID[\"EPSG\", 4326]],\n"
                        + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion (Scaled)\",\n"
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
                        + "    BBOX[-90, -180, 90, 180]]"));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testBTE3() {
        testProjectionAccuracy(
                EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        //TODO: this would be nicer using DERIVEDPROJCRS from WKT2:2019
                        "FITTED_CS[\"WGS 84 / Reversed Axis Order / BuildTheEarth Conformal Dymaxion (Scaled)\",\n"
                        + "    PARAM_MT[\"Affine\",\n"
                        + "        METHOD[\"Affine\", ID[\"EPSG\", 9624]],\n"
                        + "        PARAMETER[\"num_col\", 3],\n"
                        + "        PARAMETER[\"num_row\", 3],\n"
                        + "        PARAMETER[\"elt_0_0\", 1.3664447449393513E-7],\n"
                        + "        PARAMETER[\"elt_0_1\", 0],\n"
                        + "        PARAMETER[\"elt_1_0\", 0],\n"
                        + "        PARAMETER[\"elt_1_1\", 1.3664447449393513E-7]],\n"
                        + "    PROJCRS[\"WGS 84 / Reversed Axis Order / BuildTheEarth Conformal Dymaxion (Unscaled)\",\n"
                        + "        BASEGEODCRS[\"WGS 84\",\n"
                        + "            DATUM[\"World Geodetic System 1984\",\n"
                        + "                ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                    LENGTHUNIT[\"metre\",1]]],\n"
                        + "            PRIMEM[\"Greenwich\",0,\n"
                        + "                ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "        CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion (Unscaled)\",\n"
                        + "            METHOD[\"Terra++ Internal Projection\"],\n"
                        + "            PARAMETER[\"type\", \"bte_conformal_dymaxion\"]],\n"
                        + "        CS[Cartesian,2],\n"
                        + "            AXIS[\"X\",east,\n"
                        + "                ORDER[1],\n"
                        + "                LENGTHUNIT[\"metre\", 1]],\n"
                        + "            AXIS[\"Y\",south,\n"
                        + "                ORDER[2],\n"
                        + "                LENGTHUNIT[\"metre\", 1]],\n"
                        + "        SCOPE[\"Minecraft.\"],\n"
                        + "        AREA[\"World.\"],\n"
                        + "        BBOX[-90, -180, 90, 180]]]"),
                1e-8d); //this is significantly less accurate than some of the others!!!
    }

    //unfortunately, this has a fair amount of additional floating-point error (off by <= ~1e-10 degrees). maybe that's acceptable? will have to test more...
    @Test
    @SneakyThrows(ParseException.class)
    public void testBTE4() {
        testProjectionAccuracy(
                EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / BuildTheEarth Conformal Dymaxion (Scaled)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\", 0,\n"
                        + "            ANGLEUNIT[\"degree\", 0.0174532925199433]],\n"
                        + "        ID[\"EPSG\", 4326]],\n"
                        + "    CONVERSION[\"Terra++ BuildTheEarth Conformal Dymaxion (Unscaled)\",\n"
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
                        + "    BBOX[-90, -180, 90, 180]]"), 1e-10d);
    }

    @Test(expected = AssertionError.class) //This should fail, as
    @SneakyThrows(ParseException.class)
    @SuppressWarnings("deprecation")
    public void testEPSG3785IsNotActuallyEPSG3785() {
        testProjectionAccuracy(
                new EPSG3785(),
                new OffsetProjectionTransform(new ScaleProjectionTransform(new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"Popular Visualisation CRS / Mercator\",\n"
                        + "    BASEGEODCRS[\"Popular Visualisation CRS\",\n"
                        + "        DATUM[\"Popular Visualisation Datum\",\n"
                        + "            ELLIPSOID[\"Popular Visualisation Sphere\",6378137,0,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"Popular Visualisation Mercator\",\n"
                        + "        METHOD[\"Mercator (1SP) (Spherical)\",\n"
                        + "            ID[\"EPSG\",9841]],\n"
                        + "        PARAMETER[\"Latitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8801]],\n"
                        + "        PARAMETER[\"Longitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8802]],\n"
                        + "        PARAMETER[\"Scale factor at natural origin\",1,\n"
                        + "            SCALEUNIT[\"unity\",1],\n"
                        + "            ID[\"EPSG\",8805]],\n"
                        + "        PARAMETER[\"False easting\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "        AXIS[\"northing (Y)\",north,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "    SCOPE[\"Web mapping and visualisation.\"],\n"
                        + "    AREA[\"World between 85.06°S and 85.06°N.\"],\n"
                        + "    BBOX[-85.06,-180,85.06,180],\n"
                        + "    ID[\"EPSG\",3785]]"), 6.388019798183263E-6, 6.388019798183263E-6), 128.0d, 128.0d));
    }

    @Test
    @SneakyThrows(ParseException.class)
    @SuppressWarnings("deprecation")
    public void testEPSG3785SameAs3857() {
        testProjectionAccuracy(
                new EPSG3785(),
                new OffsetProjectionTransform(new ScaleProjectionTransform(new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Pseudo-Mercator\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"unnamed\",\n"
                        + "        METHOD[\"Popular Visualisation Pseudo Mercator\",\n"
                        + "            ID[\"EPSG\",1024]],\n"
                        + "        PARAMETER[\"Latitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8801]],\n"
                        + "        PARAMETER[\"Longitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8802]],\n"
                        + "        PARAMETER[\"False easting\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "        AXIS[\"northing (Y)\",north,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "    SCOPE[\"Web mapping and visualisation.\"],\n"
                        + "    AREA[\"World between 85.06°S and 85.06°N.\"],\n"
                        + "    BBOX[-85.06,-180,85.06,180],\n"
                        + "    ID[\"EPSG\",3857]]"), 6.388019798183263E-6, 6.388019798183263E-6), 128.0d, 128.0d),
                1e-12d); //this is slightly less accurate

        testProjectionAccuracy(
                new EPSG3785(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Terra++ Scaled Pseudo-Mercator\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"unnamed\",\n"
                        + "        METHOD[\"Popular Visualisation Pseudo Mercator\",\n"
                        + "            ID[\"EPSG\",1024]],\n"
                        + "        PARAMETER[\"Latitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8801]],\n"
                        + "        PARAMETER[\"Longitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8802]],\n"
                        //porkman added this: begin
                        + "        PARAMETER[\"Scale factor at natural origin\",6.388019798183263E-6,\n"
                        + "            SCALEUNIT[\"unity\",1],\n"
                        + "            ID[\"EPSG\",8805]],\n"
                        //porkman added this: end
                        //porkman changed these parameter values from 0 to 128: begin
                        + "        PARAMETER[\"False easting\",128.0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",128.0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        //porkman changed these parameter values from 0 to 128: end
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "        AXIS[\"northing (Y)\",north,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "    SCOPE[\"Web mapping and visualisation.\"],\n"
                        + "    AREA[\"World between 85.06°S and 85.06°N.\"],\n"
                        + "    BBOX[-85.06,-180,85.06,180]]"),
                1e-12d); //this is slightly less accurate
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testWebMercatorSameAs3857() {
        testProjectionAccuracy(
                new WebMercatorProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Terra++ Scaled Pseudo-Mercator (Web Mercator)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"unnamed\",\n"
                        + "        METHOD[\"Popular Visualisation Pseudo Mercator\",\n"
                        + "            ID[\"EPSG\",1024]],\n"
                        + "        PARAMETER[\"Latitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8801]],\n"
                        + "        PARAMETER[\"Longitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8802]],\n"
                        //porkman added this: begin
                        + "        PARAMETER[\"Scale factor at natural origin\",6.388019798183263E-6,\n"
                        + "            SCALEUNIT[\"unity\",1],\n"
                        + "            ID[\"EPSG\",8805]],\n"
                        //porkman added this: end
                        //porkman changed these parameter values from 0 to 128: begin
                        + "        PARAMETER[\"False easting\",128.0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",-128.0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        //porkman changed these parameter values from 0 to 128: end
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "        AXIS[\"southing (Y)\",south,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "    SCOPE[\"Web mapping and visualisation.\"],\n"
                        + "    AREA[\"World between 85.06°S and 85.06°N.\"],\n"
                        + "    BBOX[-85.06,-180,85.06,180]]"),
                1e-12d); //this is slightly less accurate
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testEPSG4326AgainstReal() {
        testProjectionAccuracy(
                new SwapAxesProjectionTransform(new EPSG4326()),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "GEODCRS[\"WGS 84\",\n"
                        + "    DATUM[\"World Geodetic System 1984\",\n"
                        + "        ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "            LENGTHUNIT[\"metre\",1]]],\n"
                        + "    PRIMEM[\"Greenwich\",0,\n"
                        + "        ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    CS[ellipsoidal,2],\n"
                        + "        AXIS[\"geodetic latitude (Lat)\",north,\n"
                        + "            ORDER[1],\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "        AXIS[\"geodetic longitude (Lon)\",east,\n"
                        + "            ORDER[2],\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180],\n"
                        + "    ID[\"EPSG\",4326]]"));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testEPSG4326AgainstSwappedAxes() {
        testProjectionAccuracy(
                new EPSG4326(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "GEODCRS[\"WGS 84 / Reversed Axis Order\",\n"
                        + "    DATUM[\"World Geodetic System 1984 / Reversed Axis Order\",\n"
                        + "        ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "            LENGTHUNIT[\"metre\",1]]],\n"
                        + "    PRIMEM[\"Greenwich\",0,\n"
                        + "        ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    CS[ellipsoidal,2],\n"
                        + "        AXIS[\"geodetic latitude (Lat)\",north,\n"
                        + "            ORDER[2],\n" //porkman was here: changed this to 2 from 1
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "        AXIS[\"geodetic longitude (Lon)\",east,\n"
                        + "            ORDER[1],\n" //porkman was here: changed this to 1 from 2
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180]]"));

        testProjectionAccuracy(
                new EPSG4326(),
                new SISProjectionWrapper(TPP_GEO_CRS));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testEquirectangular() {
        testProjectionAccuracy(
                new EquirectangularProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "GEODCRS[\"WGS 84 / Reversed Axis Order / Terra++ Equirectangular\",\n"
                        + "    DATUM[\"World Geodetic System 1984 / Reversed Axis Order\",\n"
                        + "        ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "            LENGTHUNIT[\"metre\",1]]],\n"
                        + "    PRIMEM[\"Greenwich\",0,\n"
                        + "        ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    CS[ellipsoidal,2],\n"
                        + "        AXIS[\"geodetic latitude (Lat)\",north,\n"
                        + "            ORDER[2],\n" //porkman was here: changed this to 2 from 1
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "        AXIS[\"geodetic longitude (Lon)\",east,\n"
                        + "            ORDER[1],\n" //porkman was here: changed this to 1 from 2
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180]]"));

        testProjectionAccuracy(
                new EquirectangularProjection(),
                new SISProjectionWrapper(TPP_GEO_CRS));
    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testSinusoidal() {
        testProjectionAccuracy(
                new SinusoidalProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / Terra++ Sinusoidal (Unscaled)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"unnamed\",\n"
                        + "        METHOD[\"Pseudo sinusoidal\"],\n"
                        + "        PARAMETER[\"False easting\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"unnamed\", 111319.49079327358]],\n"
                        + "        AXIS[\"northing (Y)\",north,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"unnamed\", 111319.49079327358]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180]]"),
                1e-10d); //this is slightly less accurate than some of the others

        testProjectionAccuracy(
                new SinusoidalProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        //TODO: this would be nicer using DERIVEDPROJCRS from WKT2:2019
                        "FITTED_CS[\"WGS 84 / Reversed Axis Order / Terra++ Sinusoidal (Degrees)\",\n"
                        + "    PARAM_MT[\"Affine\",\n"
                        + "        METHOD[\"Affine\", ID[\"EPSG\", 9624]],\n"
                        + "        PARAMETER[\"num_col\", 3],\n"
                        + "        PARAMETER[\"num_row\", 3],\n"
                        + "        PARAMETER[\"elt_0_0\", 111319.49079327358],\n"
                        + "        PARAMETER[\"elt_0_1\", 0],\n"
                        + "        PARAMETER[\"elt_1_0\", 0],\n"
                        + "        PARAMETER[\"elt_1_1\", 111319.49079327358]],\n"
                        + "    PROJCRS[\"WGS 84 / Reversed Axis Order / Terra++ Sinusoidal (Radians)\",\n"
                        + "        BASEGEODCRS[\"WGS 84\",\n"
                        + "            DATUM[\"World Geodetic System 1984\",\n"
                        + "                ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                    LENGTHUNIT[\"metre\",1]]],\n"
                        + "            PRIMEM[\"Greenwich\",0,\n"
                        + "                ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "        CONVERSION[\"unnamed\",\n"
                        + "            METHOD[\"Pseudo sinusoidal\"],\n"
                        + "            PARAMETER[\"False easting\",0,\n"
                        + "                LENGTHUNIT[\"metre\",1],\n"
                        + "                ID[\"EPSG\",8806]],\n"
                        + "            PARAMETER[\"False northing\",0,\n"
                        + "                LENGTHUNIT[\"metre\",1],\n"
                        + "                ID[\"EPSG\",8807]]],\n"
                        + "        CS[Cartesian,2],\n"
                        + "            AXIS[\"easting (X)\",east,\n"
                        + "                ORDER[1],\n"
                        + "                LENGTHUNIT[\"metre\", 1]],\n"
                        + "            AXIS[\"northing (Y)\",north,\n"
                        + "                ORDER[2],\n"
                        + "                LENGTHUNIT[\"metre\", 1]],\n"
                        + "        SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "        AREA[\"World.\"],\n"
                        + "        BBOX[-90,-180,90,180]]]"),
                1e-10d); //this is slightly less accurate than some of the others

        testProjectionAccuracy(
                new SinusoidalProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Reversed Axis Order / Terra++ Sinusoidal\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"Terra++ Sinusoidal\",\n"
                        + "        METHOD[\"Terra++ Sinusoidal\"]],\n"
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\", 1]],\n"
                        + "        AXIS[\"northing (Y)\",north,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\", 1]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180]]"),
                1e-10d); //this is slightly less accurate than some of the others

    }

    @Test
    @SneakyThrows(ParseException.class)
    public void testCenteredMercator() {
        testProjectionAccuracy(
                new CenteredMercatorProjection(),
                new SISProjectionWrapper(WKTStandard.WKT2_2015,
                        "PROJCRS[\"WGS 84 / Terra++ Scaled Centered Mercator (Pseudo-Mercator)\",\n"
                        + "    BASEGEODCRS[\"WGS 84\",\n"
                        + "        DATUM[\"World Geodetic System 1984\",\n"
                        + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
                        + "                LENGTHUNIT[\"metre\",1]]],\n"
                        + "        PRIMEM[\"Greenwich\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
                        + "    CONVERSION[\"unnamed\",\n"
                        + "        METHOD[\"Popular Visualisation Pseudo Mercator\",\n"
                        + "            ID[\"EPSG\",1024]],\n"
                        + "        PARAMETER[\"Latitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8801]],\n"
                        + "        PARAMETER[\"Longitude of natural origin\",0,\n"
                        + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
                        + "            ID[\"EPSG\",8802]],\n"
                        //porkman added this: begin
                        + "        PARAMETER[\"Scale factor at natural origin\",4.990640467330674E-8,\n"
                        + "            SCALEUNIT[\"unity\",1],\n"
                        + "            ID[\"EPSG\",8805]],\n"
                        //porkman added this: end
                        + "        PARAMETER[\"False easting\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8806]],\n"
                        + "        PARAMETER[\"False northing\",0,\n"
                        + "            LENGTHUNIT[\"metre\",1],\n"
                        + "            ID[\"EPSG\",8807]]],\n"
                        + "    CS[Cartesian,2],\n"
                        + "        AXIS[\"easting (X)\",east,\n"
                        + "            ORDER[1],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "        AXIS[\"southing (Y)\",south,\n"
                        + "            ORDER[2],\n"
                        + "            LENGTHUNIT[\"metre\",1]],\n"
                        + "    SCOPE[\"Horizontal component of 3D system.\"],\n"
                        + "    AREA[\"World.\"],\n"
                        + "    BBOX[-90,-180,90,180]]"),
                1e-13d); //this is slightly less accurate
    }

    private static boolean approxEquals(double[] a, double[] b) {
        return approxEquals(a, b, DEFAULT_D);
    }

    private static boolean approxEquals(double[] a, double[] b, double d) {
        //noinspection ArrayEquality
        if (a == b) {
            return true;
        } else if (a == null || b == null || a.length != b.length) {
            return false;
        }

        for (int i = 0, len = a.length; i < len; i++) {
            if (!approxEquals(a[i], b[i], d)) {
                return false;
            }
        }

        return true;
    }

    private static boolean approxEquals(Matrix2 a, Matrix2 b) {
        return approxEquals(a, b, DEFAULT_D);
    }

    private static boolean approxEquals(Matrix2 a, Matrix2 b, double d) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        return approxEquals(a.getElements(), b.getElements(), d);
    }

    private static boolean veryApproximateEquals(Matrix2 a, Matrix2 b, double maxErrorInPercent) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                double da = a.getElement(row, col);
                double db = b.getElement(row, col);
                if (Math.abs(da - db) / Math.max(Math.abs(da), Math.abs(db)) >= maxErrorInPercent) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean approxEquals(double a, double b) {
        return approxEquals(a, b, DEFAULT_D);
    }

    private static boolean approxEquals(double a, double b, double d) {
        return Math.abs(a - b) < d;
    }
}
