package io.github.terra121.util;

import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PorkUtil;

import java.util.Comparator;
import java.util.Objects;

/**
 * Copied from <a href="https://github.com/PorkStudios/FarPlaneTwo/blob/2dfd92f/src/main/java/net/daporkchop/fp2/util/EqualsTieBreakComparator.java">FarPlaneTwo</a>.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class EqualsTieBreakComparator<T> implements Comparator<T> {
    protected final Comparator<T> comparator;
    protected final boolean useHashCode;
    protected final boolean up;

    @Override
    public int compare(T o1, T o2) {
        int d;
        if (this.comparator != null) {
            d = this.comparator.compare(o1, o2);
        } else {
            d = PorkUtil.<Comparable<T>>uncheckedCast(o1).compareTo(o2);
        }

        if (d == 0 && !Objects.equals(o1, o2)) { //comparison resulted in a tie, but the objects are different
            if (this.useHashCode) {
                d = Integer.compare(Objects.hashCode(o1), Objects.hash(o2));
            }
            if (d == 0) {
                d = this.up ? 1 : -1;
            }
        }
        return d;
    }
}
