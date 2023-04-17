package net.buildtheearth.terraplusplus.util.geo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
public abstract class GeographicCoordinates implements Internable<GeographicCoordinates> {
    private static final GeographicCoordinates ZERO = fromLatLonDegrees(0.0d, 0.0d);

    /**
     * @return an instance of {@link GeographicCoordinates} with a latitude and longitude of {@code 0.0d}
     */
    public static GeographicCoordinates zero() {
        return ZERO;
    }

    public static GeographicCoordinates fromLatLonDegrees(double latitude, double longitude) {
        return new InDegrees(latitude, longitude);
    }

    public static GeographicCoordinates fromLonLatDegrees(double longitude, double latitude) {
        return fromLatLonDegrees(latitude, longitude);
    }

    public static GeographicCoordinates fromLatLonRadians(double latitude, double longitude) {
        return new InDegrees(latitude, longitude);
    }

    public static GeographicCoordinates fromLonLatRadians(double longitude, double latitude) {
        return fromLatLonRadians(latitude, longitude);
    }

    protected final double latitude;
    protected final double longitude;

    public abstract double latitudeDegrees();

    public abstract double longitudeDegrees();

    public abstract GeographicCoordinates withLatitudeDegrees(double latitudeDegrees);

    public abstract GeographicCoordinates withLongitudeDegrees(double longitudeDegrees);

    public abstract double latitudeRadians();

    public abstract double longitudeRadians();

    public abstract GeographicCoordinates withLatitudeRadians(double latitudeRadians);

    public abstract GeographicCoordinates withLongitudeRadians(double longitudeRadians);

    /**
     * @return {@code true} if both the latitude and longitude are equal to {@code 0.0d}
     */
    public boolean isZero() {
        return this.latitude == 0.0d && this.longitude == 0.0d;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (this.getClass() == obj.getClass()) {
            GeographicCoordinates other = (GeographicCoordinates) obj;
            return this.latitude == other.latitude && this.longitude == other.longitude;
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Double.hashCode(this.latitude) * 31 + Double.hashCode(this.longitude);
    }

    @Override
    public abstract String toString();

    @Override
    public GeographicCoordinates intern() {
        return InternHelper.intern(this);
    }

    private static final class InDegrees extends GeographicCoordinates {
        private InDegrees(double latitudeDegrees, double longitudeDegrees) {
            super(latitudeDegrees, longitudeDegrees);
        }

        @Override
        public double latitudeDegrees() {
            return this.latitude;
        }

        @Override
        public double longitudeDegrees() {
            return this.longitude;
        }

        @Override
        public GeographicCoordinates withLatitudeDegrees(double latitudeDegrees) {
            return latitudeDegrees != this.latitude
                    ? new InDegrees(latitudeDegrees, this.longitude)
                    : this;
        }

        @Override
        public GeographicCoordinates withLongitudeDegrees(double longitudeDegrees) {
            return longitudeDegrees != this.longitude
                    ? new InDegrees(this.latitude, longitudeDegrees)
                    : this;
        }

        @Override
        public double latitudeRadians() {
            return toRadians(this.latitude);
        }

        @Override
        public double longitudeRadians() {
            return toRadians(this.longitude);
        }

        @Override
        public GeographicCoordinates withLatitudeRadians(double latitudeRadians) {
            return this.withLatitudeDegrees(toDegrees(latitudeRadians));
        }

        @Override
        public GeographicCoordinates withLongitudeRadians(double longitudeRadians) {
            return this.withLongitudeDegrees(toDegrees(longitudeRadians));
        }

        @Override
        public String toString() {
            return "EllipsoidalCoordinates(latitude=" + this.latitude + "°, longitude=" + this.longitude + "°)";
        }
    }

    private static final class InRadians extends GeographicCoordinates {
        private InRadians(double latitudeRadians, double longitudeRadians) {
            super(latitudeRadians, longitudeRadians);
        }

        @Override
        public double latitudeRadians() {
            return this.latitude;
        }

        @Override
        public double longitudeRadians() {
            return this.longitude;
        }

        @Override
        public GeographicCoordinates withLatitudeRadians(double latitudeRadians) {
            return latitudeRadians != this.latitude
                    ? new InRadians(latitudeRadians, this.longitude)
                    : this;
        }

        @Override
        public GeographicCoordinates withLongitudeRadians(double longitudeRadians) {
            return longitudeRadians != this.longitude
                    ? new InRadians(this.latitude, longitudeRadians)
                    : this;
        }

        @Override
        public double latitudeDegrees() {
            return toDegrees(this.latitude);
        }

        @Override
        public double longitudeDegrees() {
            return toDegrees(this.longitude);
        }

        @Override
        public GeographicCoordinates withLatitudeDegrees(double latitudeDegrees) {
            return this.withLatitudeRadians(toRadians(latitudeDegrees));
        }

        @Override
        public GeographicCoordinates withLongitudeDegrees(double longitudeDegrees) {
            return this.withLongitudeRadians(toRadians(longitudeDegrees));
        }

        @Override
        public String toString() {
            return "EllipsoidalCoordinates(latitude=" + this.latitude + " rad, longitude=" + this.longitude + " rad)";
        }
    }
}
