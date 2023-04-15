package net.buildtheearth.terraplusplus.projection.wkt;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.crs.CRS;
import net.buildtheearth.terraplusplus.crs.GeocentricCRS;
import net.buildtheearth.terraplusplus.crs.GeodeticCRS;
import net.buildtheearth.terraplusplus.crs.GeographicCRS;
import net.buildtheearth.terraplusplus.crs.ProjectedCRS;
import net.buildtheearth.terraplusplus.crs.cs.CartesianCS;
import net.buildtheearth.terraplusplus.crs.cs.CoordinateSystem;
import net.buildtheearth.terraplusplus.crs.cs.EllipsoidalCS;
import net.buildtheearth.terraplusplus.crs.cs.axis.Axis;
import net.buildtheearth.terraplusplus.crs.cs.axis.AxisDirection;
import net.buildtheearth.terraplusplus.crs.datum.Datum;
import net.buildtheearth.terraplusplus.crs.datum.GeodeticDatum;
import net.buildtheearth.terraplusplus.crs.datum.ellipsoid.Ellipsoid;
import net.buildtheearth.terraplusplus.crs.operation.Projection;
import net.buildtheearth.terraplusplus.crs.unit.DefaultUnits;
import net.buildtheearth.terraplusplus.crs.unit.Unit;
import net.buildtheearth.terraplusplus.crs.unit.UnitType;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCompoundCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTProjectedCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTStaticGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTAxis;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTStaticGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTPrimeMeridian;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjection;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;
import net.daporkchop.lib.common.util.PorkUtil;

/**
 * @author DaPorkchop_
 */
@SuppressWarnings("UnstableApiUsage")
@UtilityClass
public class WKTToTPPConverter {
    public static CRS convertCRS(@NonNull WKTCRS crs) {
        if (crs instanceof WKTCRS.WithCoordinateSystem) {
            return convertCRS((WKTCRS.WithCoordinateSystem) crs);
        } else if (crs instanceof WKTCompoundCRS) {
            return convertCRS((WKTCompoundCRS) crs);
        } else {
            throw new IllegalArgumentException(PorkUtil.className(crs));
        }
    }

    public static CRS convertCRS(@NonNull WKTCRS.WithCoordinateSystem crs) {
        if (crs instanceof WKTGeographicCRS) {
            return convertCRS((WKTGeographicCRS) crs);
        } else if (crs instanceof WKTProjectedCRS) {
            return convertCRS((WKTProjectedCRS) crs);
        } else { //TODO
            throw new IllegalArgumentException(PorkUtil.className(crs));
        }
    }

    public static GeodeticCRS convertCRS(@NonNull WKTGeographicCRS crs) {
        GeodeticDatum convertedDatum = (GeodeticDatum) convertDatum(((WKTStaticGeographicCRS) crs).datum());
        CoordinateSystem convertedCS = convertCS(crs.coordinateSystem());

        if (convertedCS instanceof EllipsoidalCS) {
            return new GeographicCRS(convertedDatum, (EllipsoidalCS) convertedCS);
        } else if (convertedCS instanceof CartesianCS) {
            return new GeocentricCRS(convertedDatum, (CartesianCS) convertedCS);
        } else { //TODO
            throw new IllegalArgumentException(PorkUtil.className(convertedCS));
        }
    }

    public static ProjectedCRS convertCRS(@NonNull WKTProjectedCRS crs) {
        GeographicCRS convertedBaseCRS = (GeographicCRS) convertCRS(crs.baseCrs());
        CartesianCS convertedCS = (CartesianCS) convertCS(crs.coordinateSystem());
        throw new UnsupportedOperationException(); //TODO
    }

    public static Projection convertProjection(@NonNull WKTProjection projection) {
    }

    public static CRS convertCRS(@NonNull WKTCompoundCRS crs) {
        throw new UnsupportedOperationException(); //TODO
    }

    public static Datum convertDatum(@NonNull WKTDatum datum) {
        if (datum instanceof WKTGeodeticDatum) {
            return convertDatum((WKTGeodeticDatum) datum);
        } else { //TODO
            throw new IllegalArgumentException(PorkUtil.className(datum));
        }
    }

    public static Datum convertDatum(@NonNull WKTGeodeticDatum datum) {
        if (datum instanceof WKTStaticGeodeticDatum) {
            return convertDatum((WKTStaticGeodeticDatum) datum);
        } else { //TODO
            throw new IllegalArgumentException(PorkUtil.className(datum));
        }
    }

    public static GeodeticDatum convertDatum(@NonNull WKTStaticGeodeticDatum datum) {
        return new GeodeticDatum(
                convertEllipsoid(datum.ellipsoid()),
                datum.primeMeridian() != null ? convertPrimeMeridian(datum.primeMeridian()) : new GeodeticDatum.PrimeMeridian(DefaultUnits.radian(), 0.0d));
    }

    public static Ellipsoid convertEllipsoid(@NonNull WKTEllipsoid ellipsoid) {
        Unit convertedUnit = ellipsoid.unit() != null ? convertUnit(ellipsoid.unit()) : DefaultUnits.meter();
        double semiMajorAxis = ellipsoid.semiMajorAxis().doubleValue();

        if (ellipsoid.isSphere()) {
            return Ellipsoid.createSphere(convertedUnit, semiMajorAxis);
        } else if (ellipsoid.inverseFlattening() != null) { //ellipsoid is defined by its inverse flattening
            return Ellipsoid.createFromInverseFlattening(convertedUnit, semiMajorAxis, ellipsoid.inverseFlattening().doubleValue());
        } else { //ellipsoid is defined by its semi-minor axis
            return Ellipsoid.createFromAxes(convertedUnit, semiMajorAxis, ellipsoid.semiMinorAxis().doubleValue());
        }
    }

    public static GeodeticDatum.PrimeMeridian convertPrimeMeridian(@NonNull WKTPrimeMeridian primeMeridian) {
        return new GeodeticDatum.PrimeMeridian(
                convertUnit(primeMeridian.longitude().unit()),
                primeMeridian.longitude().value().doubleValue());
    }

    public static CoordinateSystem convertCS(@NonNull WKTCS cs) {
        ImmutableList<Axis> convertedAxes = cs.axes().stream().map(WKTToTPPConverter::convertAxis).collect(ImmutableList.toImmutableList());

        //TODO: use the CS unit somehow

        switch (cs.type()) {
            case ellipsoidal:
                return new EllipsoidalCS(convertedAxes);
            case Cartesian:
                return new CartesianCS(convertedAxes);
        }

        throw new IllegalArgumentException(cs.toString());
    }

    public static Axis convertAxis(@NonNull WKTAxis axis) {
        Unit convertedUnit = convertUnit(axis.unit());

        Number minimum = null;
        Number maximum = null;
        boolean wrapAround = false;

        if (convertedUnit.type() == UnitType.ANGLE) {
            switch (axis.direction()) {
                case north:
                case south: {
                    double range = Math.abs(DefaultUnits.radian().convertTo(convertedUnit).convert(Math.toRadians(90.0d)));
                    minimum = -range;
                    maximum = range;
                    wrapAround = false;
                    break;
                }
                case east:
                case west: {
                    double range = Math.abs(DefaultUnits.radian().convertTo(convertedUnit).convert(Math.toRadians(180.0d)));
                    minimum = -range;
                    maximum = range;
                    wrapAround = true;
                    break;
                }
            }
        }

        return new Axis(axis.name(),
                convertAxisDirection(axis.direction()),
                convertedUnit,
                minimum, maximum, wrapAround);
    }

    private static AxisDirection convertAxisDirection(@NonNull WKTAxis.Direction direction) {
        StringBuilder builder = new StringBuilder();

        String origName = direction.name();
        for (int i = 0; i < origName.length(); i++) {
            char origC = origName.charAt(i);
            char convertedC = Character.toUpperCase(origC);

            if (origC == convertedC) {
                builder.append('_');
            }
            builder.append(convertedC);
        }

        return AxisDirection.valueOf(builder.toString());
    }

    public static Unit convertUnit(@NonNull WKTUnit unit) {
        Unit baseUnit;
        if (unit instanceof WKTLengthUnit) {
            baseUnit = DefaultUnits.meter();
        } else if (unit instanceof WKTAngleUnit) {
            baseUnit = DefaultUnits.radian();
        } else {
            throw new IllegalArgumentException(PorkUtil.className(unit));
        }

        return baseUnit.multiply(unit.conversionFactor())
                .withName(unit.name());
    }
}
