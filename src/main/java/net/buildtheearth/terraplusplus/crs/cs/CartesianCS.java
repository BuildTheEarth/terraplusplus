package net.buildtheearth.terraplusplus.crs.cs;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.cs.axis.Axis;
import net.buildtheearth.terraplusplus.crs.cs.axis.AxisDirection;
import net.buildtheearth.terraplusplus.crs.unit.DefaultUnits;
import net.buildtheearth.terraplusplus.crs.unit.Unit;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.TerraUtils;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) //only accessible by @With, skips argument validation
@Getter
@With(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY, callSuper = true)
public final class CartesianCS extends AbstractCoordinateSystem {
    @NonNull
    @ToString.Include
    @EqualsAndHashCode.Include
    private final ImmutableList<Axis> axes;

    private transient final int dimension;

    public CartesianCS(@NonNull ImmutableList<Axis> axes) {
        this.axes = initialValidateAxes(axes, this);
        this.dimension = axes.size();
    }

    @Override
    protected boolean permittedDirection(@NonNull AxisDirection direction) {
        return !AxisDirection.FUTURE.equals(direction.absolute());
    }

    @Override
    protected boolean permittedUnit(@NonNull AxisDirection direction, @NonNull Unit unit) {
        return DefaultUnits.meter().compatibleWith(unit);
    }

    @Override
    public CartesianCS intern() {
        return InternHelper.intern(this.withAxes(TerraUtils.internElements(this.axes)));
    }
}
