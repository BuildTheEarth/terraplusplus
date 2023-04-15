package net.buildtheearth.terraplusplus.crs.operation;

/**
 * A {@link Conversion} which transforms {@code (longitude, latitude)} coordinates to {@code (x, y)} cartesian coordinates.
 *
 * @author DaPorkchop_
 */
public interface Projection extends Conversion {
    @Override
    Projection intern();
}
