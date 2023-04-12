package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCompoundCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTAxis;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;
import net.daporkchop.lib.common.util.PorkUtil;

import java.util.Set;
import java.util.stream.Collectors;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class WKTToTPPConverter {
    public static GeographicProjection convertToGeographicProjection(@NonNull WKTCRS crs) {
        if (crs instanceof WKTCRS.WithCoordinateSystem) {
            return convertToGeographicProjection((WKTCRS.WithCoordinateSystem) crs);
        } else if (crs instanceof WKTCompoundCRS) {
            return convertToGeographicProjection((WKTCompoundCRS) crs);
        } else {
            throw new IllegalArgumentException(PorkUtil.className(crs));
        }
    }

    public static GeographicProjection convertToGeographicProjection(@NonNull WKTCRS.WithCoordinateSystem crs) {
        if (crs instanceof WKTGeographicCRS) {
            return convertToGeographicProjection((WKTGeographicCRS) crs);
        } else { //TODO
            throw new IllegalArgumentException(PorkUtil.className(crs));
        }
    }

    public static GeographicProjection convertToGeographicProjection(@NonNull WKTGeographicCRS crs) {
        WKTCS coordinateSystem = crs.coordinateSystem();

        checkState(coordinateSystem.type() == WKTCS.Type.ellipsoidal, "unexpected coordinate system type: %s", coordinateSystem.type());
        checkState(coordinateSystem.axes().size() == 2 || coordinateSystem.axes().size() == 3, "unexpected coordinate system axis count: %d", coordinateSystem.axes().size());

        Set<WKTAxis.Direction> axisDirections = coordinateSystem.axes().stream().map(WKTAxis::direction).collect(Collectors.toSet());
        checkState(axisDirections.contains(null)); //TODO

        switch (coordinateSystem.axes().size()) {
            case 2:
                return null;
        }

        throw new UnsupportedOperationException(); //TODO
    }

    public static GeographicProjection convertToGeographicProjection(@NonNull WKTCompoundCRS crs) {
        throw new UnsupportedOperationException(); //TODO
    }
}
