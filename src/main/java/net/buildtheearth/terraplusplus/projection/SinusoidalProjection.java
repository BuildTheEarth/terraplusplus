package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of the Sinusoidal projection.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Sinusoidal_projection"> Wikipedia's article on the sinusoidal projection</a>
 */
@JsonDeserialize
public class SinusoidalProjection implements GeographicProjection {
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final CoordinateReferenceSystem PROJECTED_CRS = (CoordinateReferenceSystem) WKTStandard.WKT2_2015.parseUnchecked(
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
            + "        BBOX[-90,-180,90,180]]]");

    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{ x / Math.cos(Math.toRadians(y)), y };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
    	OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude * Math.cos(Math.toRadians(latitude)), latitude };
    }

    @Override
    public double[] bounds() {
        return this.boundsGeo();
    }

    @Override
    public CoordinateReferenceSystem projectedCRS() {
        return PROJECTED_CRS();
    }

    @Override
    public String toString() {
        return "Sinusoidal";
    }
}
