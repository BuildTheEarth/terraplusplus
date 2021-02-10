package net.buildtheearth.terraplusplus.projection;

/**
 * A function that can apply a projection to a pair of lon/lat or x/y coordinates.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface ProjectionFunction {
    double[] project(double lon_x, double lat_y) throws OutOfProjectionBoundsException;
}
