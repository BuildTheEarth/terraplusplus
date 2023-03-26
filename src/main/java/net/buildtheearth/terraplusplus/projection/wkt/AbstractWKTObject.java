package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTID;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class AbstractWKTObject implements WKTObject {
    @Override
    public final String toString() {
        return this.toString(WKTStyle.ONE_LINE);
    }

    @Override
    public final String toPrettyString() {
        return this.toString(WKTStyle.PRETTY);
    }

    @Override
    @SneakyThrows(IOException.class)
    public final String toString(@NonNull WKTStyle style) {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, style)) {
            this.write(writer);
        }
        return builder.toString();
    }

    @Override
    public abstract void write(@NonNull WKTWriter writer) throws IOException;

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithID extends AbstractWKTObject implements WKTObject.WithID {
        protected static final WKTParseSchema<AbstractWKTObject.WithID> BASE_PARSE_SCHEMA = WKTParseSchema.<AbstractWKTObject.WithID, WithIDBuilder<AbstractWKTObject.WithID, ?>>builder(() -> null, WithIDBuilder::build)
                .permitKeyword("")
                .optionalObjectProperty(WKTID.PARSE_SCHEMA, WithIDBuilder::id)
                .build();

        @Builder.Default
        private final WKTID id = null;
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithName extends AbstractWKTObject implements WKTObject.WithName {
        @NonNull
        private final String name;
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithNameAndID extends AbstractWKTObject.WithID implements WKTObject.WithName {
        @NonNull
        private final String name;
    }
}
