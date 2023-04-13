package net.buildtheearth.terraplusplus.crs.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;
import net.buildtheearth.terraplusplus.util.InternHelper;

/**
 * Base implementation of {@link UnitConverter}.
 *
 * @author DaPorkchop_
 */
public abstract class AbstractUnitConverter implements UnitConverter {
    @Override
    public abstract boolean isIdentity();

    @Override
    public abstract double convert(double value);

    @Override
    public final UnitConverter inverse() {
        if (this.isIdentity()) {
            return UnitConverterIdentity.instance();
        }

        return this.inverse0();
    }

    /**
     * Gets an {@link UnitConverter} which performs the inverse of this converter's operation.
     * <p>
     * The user may assume that this {@link AbstractUnitConverter} is not {@link #isIdentity() an identity conversion}.
     *
     * @return an {@link UnitConverter} which performs the inverse of this converter's operation
     */
    protected abstract UnitConverter inverse0();

    @Override
    public final UnitConverter intern() {
        if (this.isIdentity()) {
            return UnitConverterIdentity.instance().intern();
        }

        return InternHelper.intern(this.withChildrenInterned());
    }

    protected UnitConverter withChildrenInterned() {
        return this;
    }

    @Override
    public final UnitConverter simplify() {
        if (this.isIdentity()) {
            return UnitConverterIdentity.instance();
        }

        return this.simplify0();
    }

    /**
     * Gets an {@link UnitConverter} which is equivalent to this one, but may be able to execute more efficiently.
     * <p>
     * The user may assume that this {@link AbstractUnitConverter} is not {@link #isIdentity() an identity conversion}.
     *
     * @return an {@link UnitConverter} which is equivalent to this one, but may be able to execute more efficiently
     */
    protected abstract UnitConverter simplify0();

    @Override
    public final UnitConverter andThen(@NonNull UnitConverter next) {
        if (this.isIdentity()) {
            return next;
        } else if (next.isIdentity()) {
            return this;
        }

        UnitConverter result = this.tryAndThen(next);
        if (result != null) {
            return result;
        }

        return new UnitConverterSequence(ImmutableList.of(this, next));
    }

    protected UnitConverter tryAndThen(@NonNull UnitConverter next) {
        return null;
    }

    /**
     * An {@link UnitConverter} which actually represents multiple {@link UnitConverter}s combined into one, but may be "un-combined" into
     * its component steps to allow for further optimization.
     *
     * @author DaPorkchop_
     */
    public interface RepresentableAsSequence {
        UnitConverterSequence asConverterSequence();
    }
}
