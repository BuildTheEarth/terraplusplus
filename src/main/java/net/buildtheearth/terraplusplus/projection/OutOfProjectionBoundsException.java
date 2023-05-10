package net.buildtheearth.terraplusplus.projection;

import org.apache.sis.referencing.operation.projection.ProjectionException;
import org.opengis.referencing.operation.MathTransform;

public final class OutOfProjectionBoundsException extends ProjectionException {
    private static final OutOfProjectionBoundsException INSTANCE = new OutOfProjectionBoundsException();

    public static final boolean FAST = Boolean.parseBoolean(System.getProperty("terraplusplus.fastExcept", "true"));

    public static OutOfProjectionBoundsException get() {
        if (FAST) {
            return INSTANCE;
        } else {
            return new OutOfProjectionBoundsException();
        }
    }

    /**
     * @param x
     * @param y
     * @param maxX
     * @param maxY
     * @throws OutOfProjectionBoundsException if <code> Math.abs(x) > maxX || Math.abs(y) > maxY </code>
     */
    public static void checkInRange(double x, double y, double maxX, double maxY) throws OutOfProjectionBoundsException {
        if (Math.abs(x) > maxX || Math.abs(y) > maxY) {
            throw OutOfProjectionBoundsException.get();
        }
    }

    /**
     * @param longitude
     * @param latitude
     * @throws OutOfProjectionBoundsException if <code> Math.abs(longitude) > 180 || Math.abs(latitude) > 90 </code>
     */
    public static void checkLongitudeLatitudeInRange(double longitude, double latitude) throws OutOfProjectionBoundsException {
        checkInRange(longitude, latitude, 180, 90);
    }

    @Override
    public Throwable fillInStackTrace() {
        if (INSTANCE != null) { //if INSTANCE is null, we're still in the class constructor
            super.fillInStackTrace();
        }
        return this;
    }

    @Override
    public void setLastCompletedTransform(MathTransform transform) {
        if (this != INSTANCE) {
            super.setLastCompletedTransform(transform);
        }
    }
}
