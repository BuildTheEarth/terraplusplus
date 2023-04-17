package net.buildtheearth.terraplusplus.util.geo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.Internable;

import static java.lang.Math.*;

/**
 * A {@code (longitude, latitude)} coordinate pair, representing a position on the surface of an ellipsoid.
 *
 * @author DaPorkchop_
 * @implNote coordinates are represented in decimal degrees
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@With
public final class EllipsoidalCoordinates implements Internable<EllipsoidalCoordinates> {
    private static final EllipsoidalCoordinates ZERO = fromLatLonDegrees(0.0d, 0.0d);

    /**
     * @return an instance of {@link EllipsoidalCoordinates} with a {@link #latitudeDegrees() latitude} and {@link #longitudeDegrees() longitude} of {@code 0.0d}
     */
    public static EllipsoidalCoordinates zero() {
        return ZERO;
    }

    public static EllipsoidalCoordinates fromLatLonDegrees(double latitude, double longitude) {
        return new EllipsoidalCoordinates(latitude, longitude);
    }

    public static EllipsoidalCoordinates fromLatLonRadians(double latitude, double longitude) {
        return fromLatLonDegrees(toDegrees(latitude), toDegrees(longitude));
    }

    public static EllipsoidalCoordinates fromLonLatDegrees(double longitude, double latitude) {
        return fromLatLonDegrees(latitude, longitude);
    }

    public static EllipsoidalCoordinates fromLonLatRadians(double longitude, double latitude) {
        return fromLatLonRadians(latitude, longitude);
    }

    private final double latitudeDegrees;
    private final double longitudeDegrees;

    /**
     * @see #latitudeDegrees()
     */
    public double latitudeRadians() {
        return toRadians(this.latitudeDegrees);
    }

    /**
      @see #longitudeDegrees()
     */
    public double longitudeRadians() {
        return toRadians(this.longitudeDegrees);
    }

    /**
     * @see #withLatitudeDegrees(double)
     */
    public EllipsoidalCoordinates withLatitudeRadians(double latitudeRadians) {
        return this.withLatitudeDegrees(toDegrees(latitudeRadians));
    }

    /**
      @see #withLongitudeDegrees(double)
     */
    public EllipsoidalCoordinates withLongitudeRadians(double longitudeRadians) {
        return this.withLongitudeDegrees(toDegrees(longitudeRadians));
    }

    @Override
    public EllipsoidalCoordinates intern() {
        return InternHelper.intern(this);
    }
}
