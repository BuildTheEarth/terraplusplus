package net.buildtheearth.terraplusplus.crs.datum;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.datum.ellipsoid.Ellipsoid;
import net.buildtheearth.terraplusplus.crs.unit.Unit;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
@Data
@With(AccessLevel.PRIVATE)
public final class GeodeticDatum implements Datum {
    @NonNull
    private final Ellipsoid ellipsoid;

    @NonNull
    private final PrimeMeridian primeMeridian;

    @Override
    public GeodeticDatum intern() {
        return InternHelper.intern(this.withPrimeMeridian(this.primeMeridian.intern()));
    }

    /**
     * @author DaPorkchop_
     */
    @Data
    @With(AccessLevel.PRIVATE)
    public static final class PrimeMeridian implements Internable<PrimeMeridian> {
        @NonNull
        private final Unit unit;

        private final double greenwichLongitude;

        @Override
        public PrimeMeridian intern() {
            return InternHelper.intern(this.withUnit(this.unit.intern()));
        }
    }
}
