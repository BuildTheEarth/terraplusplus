package net.buildtheearth.terraplusplus.util.geo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@With
public final class ProjectedCoordinates2d implements Internable<ProjectedCoordinates2d> {
    private static final ProjectedCoordinates2d ZERO = new ProjectedCoordinates2d(0.0d, 0.0d);

    /**
     * @return an instance of {@link ProjectedCoordinates2d} with X and Y values of {@code 0.0d}
     */
    public static ProjectedCoordinates2d zero() {
        return ZERO;
    }

    public static ProjectedCoordinates2d ofXY(double x, double y) {
        return new ProjectedCoordinates2d(x, y);
    }

    private final double x;
    private final double y;

    @Override
    public ProjectedCoordinates2d intern() {
        return InternHelper.intern(this);
    }
}
