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
        if (instance instanceof String) {
            return uncheckedCast(((String) instance).intern());
        }

        return uncheckedCast(INTERNER.intern(instance));
    }

    public static String intern(@NonNull String instance) {
        return instance.intern();
    }

    public static <T extends Internable<? super T>> T tryInternNullableInternable(T instance) {
        return instance != null ? uncheckedCast(instance.intern()) : null;
    }

    public static String tryInternNullableString(String instance) {
        return instance != null ? instance.intern() : null;
    }
}
