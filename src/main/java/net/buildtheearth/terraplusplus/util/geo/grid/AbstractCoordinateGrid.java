package net.buildtheearth.terraplusplus.util.geo.grid;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractCoordinateGrid implements CoordinateGrid {
    private final CoordinateGrid parent;
    @NonNull
    private final CoordinateReferenceSystem crs;
}
