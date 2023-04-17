package projection;

import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.SISProjectionWrapper;
import net.buildtheearth.terraplusplus.projection.dymaxion.DymaxionProjection;
import org.junit.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class TestSISProjections {
    @Test
    @SneakyThrows
    public void testTest() {
        GeographicProjection projection = new SISProjectionWrapper("PROJCRS[\"OSGB36 / British National Grid\",BASEGEODCRS[\"OSGB36\",DATUM[\"Ordnance Survey of Great Britain 1936\",ELLIPSOID[\"Airy 1830\",6377563.396,299.3249646,LENGTHUNIT[\"metre\",1]]],PRIMEM[\"Greenwich\",0,ANGLEUNIT[\"degree\",0.0174532925199433]]],CONVERSION[\"British National Grid\",METHOD[\"Transverse Mercator\",ID[\"EPSG\",9807]],PARAMETER[\"Latitude of natural origin\",49,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8801]],PARAMETER[\"Longitude of natural origin\",-2,ANGLEUNIT[\"degree\",0.0174532925199433],ID[\"EPSG\",8802]],PARAMETER[\"Scale factor at natural origin\",0.9996012717,SCALEUNIT[\"unity\",1],ID[\"EPSG\",8805]],PARAMETER[\"False easting\",400000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8806]],PARAMETER[\"False northing\",-100000,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",8807]]],CS[Cartesian,2],AXIS[\"(E)\",east,ORDER[1],LENGTHUNIT[\"metre\",1]],AXIS[\"(N)\",north,ORDER[2],LENGTHUNIT[\"metre\",1]],SCOPE[\"Engineering survey, topographic mapping.\"],AREA[\"United Kingdom (UK) - offshore to boundary of UKCS within 49°45'N to 61°N and 9°W to 2°E; onshore Great Britain (England, Wales and Scotland). Isle of Man onshore.\"],BBOX[49.75,-9,61.01,2.01],ID[\"EPSG\",27700]]");

        GeographicProjection proj1 = new DymaxionProjection();
        GeographicProjection proj2 = new SISProjectionWrapper("PROJCRS[\"WGS 84 / Terra++ Dymaxion\",\n"
                                                              + "    BASEGEODCRS[\"WGS 84\",DATUM[\"World Geodetic System 1984\",ELLIPSOID[\"WGS 84\",6378137,298.257223563,LENGTHUNIT[\"metre\",1]]],PRIMEM[\"Greenwich\",0,ANGLEUNIT[\"degree\",0.0174532925199433]],CS[ellipsoidal,2],AXIS[\"geodetic latitude (Lat)\",north,ORDER[1],ANGLEUNIT[\"degree\",0.0174532925199433]],AXIS[\"geodetic longitude (Lon)\",east,ORDER[2],ANGLEUNIT[\"degree\",0.0174532925199433]],SCOPE[\"Horizontal component of 3D system.\"],AREA[\"World.\"],BBOX[-90,-180,90,180],ID[\"EPSG\",4326]],\n"
                                                              + "    CONVERSION[\"Terra++ Dymaxion\",\n"
                                                              + "        METHOD[\"Terra++ Dymaxion\"]],\n"
                                                              + "    CS[Cartesian, 2],\n"
                                                              + "    AXIS[\"X\", EAST, LENGTHUNIT[\"metre\", 1]],\n"
                                                              + "    AXIS[\"Y\", NORTH, LENGTHUNIT[\"metre\", 1]]]");

        SplittableRandom r = new SplittableRandom(1337L);
        for (int i = 0; i < 100; i++) {
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

        int i = 0;
    }
}
