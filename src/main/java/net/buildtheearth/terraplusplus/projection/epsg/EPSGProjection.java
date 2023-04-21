package net.buildtheearth.terraplusplus.projection.epsg;

import LZMA.LzmaInputStream;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import io.netty.util.AsciiString;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Base implementation of an EPSG projection.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@JsonSerialize
public abstract class EPSGProjection implements GeographicProjection {
    private static volatile Ref<ImmutableMap<Integer, CharSequence>> REGISTRY;

    public static ImmutableMap<Integer, CharSequence> registry(@NonNull WKTStandard standard) {
        checkArg(standard == WKTStandard.WKT2_2015, standard);

        Ref<ImmutableMap<Integer, CharSequence>> registry = REGISTRY;
        if (registry == null) {
            synchronized (EPSGProjection.class) {
                if ((registry = REGISTRY) == null) {
                    registry = REGISTRY = Ref.soft((IOSupplier<ImmutableMap<Integer, CharSequence>>) () -> {
                        Properties properties = new Properties();
                        try (InputStream in = new BufferedInputStream(new LzmaInputStream(EPSGProjection.class.getResourceAsStream("epsg_database_wkt2_2015.properties.lzma")), 1 << 16)) {
                            properties.load(in);
                        }

                        ImmutableSortedMap.Builder<Integer, CharSequence> builder = ImmutableSortedMap.naturalOrder();
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            builder.put(Integer.parseInt(entry.getKey().toString()), new AsciiString(entry.getValue().toString()));
                        }
                        return builder.build();
                    });
                }
            }
        }

        return registry.get();
    }

    protected final int code;

    @Override
    @JsonValue
    public String toString() {
        return "EPSG:" + this.code;
    }
}
