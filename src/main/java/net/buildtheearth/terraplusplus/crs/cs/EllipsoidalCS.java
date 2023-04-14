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
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.TerraUtils;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) //only accessible by @With, skips argument validation
@Getter
@With(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public final class EllipsoidalCS extends AbstractCoordinateSystem {
    @NonNull
    @ToString.Include
    @EqualsAndHashCode.Include
    private final ImmutableList<Axis> axes;

    private transient final int dimension;

    private transient final int longitudeAxis;
    private transient final int latitudeAxis;
    private transient final int heightAxis;

    @Getter(AccessLevel.NONE)
    private transient final UnitConverter longitudeConverter;
    @Getter(AccessLevel.NONE)
    private transient final UnitConverter latitudeConverter;
    @Getter(AccessLevel.NONE)
    private transient final UnitConverter heightConverter;

    public EllipsoidalCS(@NonNull ImmutableList<Axis> axes) {
        this.axes = initialValidateAxes(axes, this);
        this.dimension = axes.size();

        int longitudeAxis = -1;
        int latitudeAxis = -1;
        int heightAxis = -1;
        UnitConverter longitudeConverter = null;
        UnitConverter latitudeConverter = null;
        UnitConverter heightConverter = null;

        //precompute axis data
        for (int i = 0; i < axes.size(); i++) {
            Axis axis = axes.get(i);
            Unit unit = axis.unit();

            switch (axis.direction().absolute()) {
                case EAST:
                    longitudeAxis = i;
                    longitudeConverter = unit.convertTo(DefaultUnits.radian()).simplify();
                    break;
                case NORTH:
                    latitudeAxis = i;
                    latitudeConverter = unit.convertTo(DefaultUnits.radian()).simplify();
                    break;
                case UP:
                    heightAxis = i;
                    heightConverter = unit.convertTo(DefaultUnits.meter()).simplify();
                    break;
                default:
                    throw new IllegalArgumentException(axis.toString());
            }
        }

        checkArg(longitudeConverter != null && latitudeConverter != null, "at least a longitude and latitude axis must be set!");
        if (axes.size() == 3) {
            checkArg(heightConverter != null, "EllipsoidalCS with 3 axes must contain a height axis");
        }

        this.longitudeAxis = longitudeAxis;
        this.latitudeAxis = latitudeAxis;
        this.heightAxis = heightAxis;
        this.longitudeConverter = longitudeConverter;
        this.latitudeConverter = latitudeConverter;
        this.heightConverter = heightConverter;
    }

    @Override
    protected boolean permittedDirection(@NonNull AxisDirection direction) {
        direction = direction.absolute();
        return direction == AxisDirection.NORTH || direction == AxisDirection.EAST || direction == AxisDirection.UP;
    }

    @Override
    protected boolean permittedUnit(@NonNull AxisDirection direction, @NonNull Unit unit) {
        return (direction.absolute() == AxisDirection.UP ? DefaultUnits.meter() : DefaultUnits.radian()).compatibleWith(unit);
    }

    @Override
    public EllipsoidalCS intern() {
        return InternHelper.intern(this.withAxes(TerraUtils.internElements(this.axes))
                .withLongitudeConverter(this.longitudeConverter.intern())
                .withLatitudeConverter(this.latitudeConverter.intern())
                .withHeightConverter(InternHelper.tryInternNullableInternable(this.heightConverter)));
    }

    private void checkForHeight() {
        checkState(this.heightAxis >= 0, "this coordinate system doesn't contain a height axis!");
    }

    public double extractLongitude(@NonNull double[] point) {
        this.checkDimension(point.length);
        return point[this.longitudeAxis];
    }

    public void extractLongitude(int srcDimension, @NonNull double[] src, int srcOff, int srcStride, @NonNull double[] dst, int dstOff, int dstStride, int cnt) {
        this.checkDimension(srcDimension);
        for (int i = 0; i < cnt; i++, srcOff += srcStride, dstOff += dstStride) {
            dst[dstOff] = src[srcOff + this.longitudeAxis];
        }
    }

    public double extractLongitudeRadians(@NonNull double[] point) {
        return this.longitudeConverter.convert(this.extractLongitude(point));
    }

    public void extractLongitudeRadians(int srcDimension, @NonNull double[] src, int srcOff, int srcStride, @NonNull double[] dst, int dstOff, int dstStride, int cnt) {
        this.checkDimension(srcDimension);
        this.longitudeConverter.convert(src, srcOff + this.longitudeAxis, srcStride, dst, dstOff, dstStride, cnt);
    }

    public double extractLatitude(@NonNull double[] point) {
        this.checkDimension(point.length);
        return point[this.latitudeAxis];
    }

    public double extractLatitudeRadians(@NonNull double[] point) {
        return this.latitudeConverter.convert(this.extractLatitude(point));
    }

    public double extractHeight(@NonNull double[] point) {
        this.checkForHeight();
        this.checkDimension(point.length);
        return point[this.heightAxis];
    }

    public double extractHeightMeters(@NonNull double[] point) {
        return this.heightConverter.convert(this.extractHeight(point));
    }
}
