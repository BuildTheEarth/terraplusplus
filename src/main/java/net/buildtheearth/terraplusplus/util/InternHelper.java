package net.buildtheearth.terraplusplus.util;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@SuppressWarnings("UnstableApiUsage")
@UtilityClass
public class InternHelper {
    private static final Interner<Object> INTERNER = Interners.newWeakInterner();

    public static <T> T intern(@NonNull T instance) {
        return uncheckedCast(INTERNER.intern(instance));
    }
}
