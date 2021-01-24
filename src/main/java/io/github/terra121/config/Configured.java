package io.github.terra121.config;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * A type that may be defined by configuration.
 *
 * @author DaPorkchop_
 */
public interface Configured<I extends Configured<I>> {
    /**
     * Ensures that this object's state is valid.
     */
    void validate() throws IllegalStateException;

    /**
     * Optimizes this instance.
     *
     * @return an equivalent, potentially faster instance
     */
    default I optimize() {
        return uncheckedCast(this);
    }
}
