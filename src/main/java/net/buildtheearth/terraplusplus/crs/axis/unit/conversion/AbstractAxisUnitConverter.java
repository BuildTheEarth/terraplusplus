package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;
import net.buildtheearth.terraplusplus.util.InternHelper;

/**
 * Base implementation of {@link AxisUnitConverter}.
 *
 * @author DaPorkchop_
 */
public abstract class AbstractAxisUnitConverter implements AxisUnitConverter {
    protected static AxisUnitConverter maybeIntern(@NonNull AxisUnitConverter converter, boolean intern) {
        return intern ? converter.intern() : converter;
    }

    @Override
    public abstract boolean isIdentity();

    @Override
    public abstract double convert(double value);

    @Override
    public final AxisUnitConverter inverse() {
        if (this.isIdentity()) {
            return AxisUnitConverterIdentity.instance();
        }

        return this.inverse0();
    }

    /**
     * Gets an {@link AxisUnitConverter} which performs the inverse of this converter's operation.
     * <p>
     * The user may assume that this {@link AbstractAxisUnitConverter} is not {@link #isIdentity() an identity conversion}.
     *
     * @return an {@link AxisUnitConverter} which performs the inverse of this converter's operation
     */
    protected abstract AxisUnitConverter inverse0();

    @Override
    public final AxisUnitConverter intern() {
        if (this.isIdentity()) {
            return AxisUnitConverterIdentity.instance().intern();
        }

        return InternHelper.intern(this.simplify(true));
    }

    @Override
    public final AxisUnitConverter simplify() {
        if (this.isIdentity()) {
            return AxisUnitConverterIdentity.instance();
        }

        return this.simplify(false);
    }

    /**
     * Gets an {@link AxisUnitConverter} which is equivalent to this one, but may be able to execute more efficiently.
     * <p>
     * The user may assume that this {@link AbstractAxisUnitConverter} is not {@link #isIdentity() an identity conversion}.
     *
     * @param intern if {@code true}, any nested {@link AxisUnitConverter}s in the returned {@link AxisUnitConverter} must be interned
     * @return an {@link AxisUnitConverter} which is equivalent to this one, but may be able to execute more efficiently
     */
    protected abstract AxisUnitConverter simplify(boolean intern);

    @Override
    public final AxisUnitConverter andThen(@NonNull AxisUnitConverter next) {
        if (this.isIdentity()) {
            return next;
        } else if (next.isIdentity()) {
            return this;
        }

        AxisUnitConverter result = this.tryAndThen(next);
        if (result != null) {
            return result.simplify();
        }

        return new AxisUnitConverterSequence(ImmutableList.of(this, next)).simplify();
    }

    protected AxisUnitConverter tryAndThen(@NonNull AxisUnitConverter next) {
        return null;
    }
}
