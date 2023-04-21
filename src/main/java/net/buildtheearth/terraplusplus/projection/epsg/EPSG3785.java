package net.buildtheearth.terraplusplus.projection.epsg;

import lombok.AccessLevel;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of the EPSG:3785 projection.
 *
 * @author DaPorkchop_
 * @see <a href="https://epsg.io/3785>https://epsg.io/3785</a>
 * @deprecated this is totally wrong: it actually implements EPSG:3857 (not EPSG:3785), and with incorrect scaling
 */
@Deprecated
public final class EPSG3785 extends EPSGProjection {
    private static final WebMercatorProjection WEB_MERCATOR_PROJECTION = new WebMercatorProjection();

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final CoordinateReferenceSystem PROJECTED_CRS = (CoordinateReferenceSystem) WKTStandard.WKT2_2015.parseUnchecked(
            //based on EPSG:3857
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
            + "    BBOX[-85.06,-180,85.06,180]]");

    public EPSG3785() {
        super(3785);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return WEB_MERCATOR_PROJECTION.toGeo(x, 256.0d - y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return WEB_MERCATOR_PROJECTION.fromGeo(longitude, -latitude);
    }

    @Override
    public double[] bounds() {
        return WEB_MERCATOR_PROJECTION.bounds();
    }

    @Override
    public double[] boundsGeo() {
        return WEB_MERCATOR_PROJECTION.boundsGeo();
    }

    @Override
    public CoordinateReferenceSystem projectedCRS() {
        return PROJECTED_CRS();
    }
}
