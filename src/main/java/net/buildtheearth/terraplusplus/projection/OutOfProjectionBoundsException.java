package net.buildtheearth.terraplusplus.projection;

public final class OutOfProjectionBoundsException extends Exception {
    private static final OutOfProjectionBoundsException INSTANCE = new OutOfProjectionBoundsException(false);

    private static final boolean FAST = Boolean.parseBoolean(System.getProperty("terraplusplus.fastExcept", "true"));

    public static OutOfProjectionBoundsException get() {
        if (FAST) {
            return INSTANCE;
        } else {
            return new OutOfProjectionBoundsException(true);
        }
    }

    private OutOfProjectionBoundsException(boolean flag) {
        super(null, null, flag, flag);
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param maxX
     * @param maxY
     * @throws OutOfProjectionBoundsException if <code> Math.abs(x) > maxX || Math.abs(y) > maxY </code>
     */
    public static void checkInRange(double x, double y, double maxX, double maxY) throws OutOfProjectionBoundsException {
    	if(Math.abs(x) > maxX || Math.abs(y) > maxY) throw OutOfProjectionBoundsException.get();
    }
    
    /**
     * 
     * @param longitude
     * @param latitude
     * @throws OutOfProjectionBoundsException if <code> Math.abs(longitude) > 180 || Math.abs(latitude) > 90 </code>
     */
    public static void checkLongitudeLatitudeInRange(double longitude, double latitude) throws OutOfProjectionBoundsException {
    	checkInRange(longitude, latitude, 180, 90);
    }
}
