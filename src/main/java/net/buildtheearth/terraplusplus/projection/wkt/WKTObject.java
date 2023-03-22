package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class WKTObject {
    @Override
    public String toString() {
        return this.toString(" ", "", " ");
    }

    public String toPrettyString() {
        return this.toString("\n", "    ", " ");
    }

    @SneakyThrows(IOException.class)
    public String toString(@NonNull CharSequence lineBreak, @NonNull CharSequence indent, @NonNull CharSequence afterComma) {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, lineBreak, indent, afterComma)) {
            this.write(writer);
        }
        return builder.toString();
    }

    public abstract void write(@NonNull WKTWriter writer) throws IOException;

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithID extends WKTObject {
        protected static final WKTParseSchema<WithID> BASE_PARSE_SCHEMA = WKTParseSchema.<WithID, WithIDBuilder<WithID, ?>>builder(() -> null, WithIDBuilder::build)
                .permitKeyword("")
                .optionalObjectProperty(WKTID.PARSE_SCHEMA, WithIDBuilder::id)
                .build();

        @Builder.Default
        private final WKTID id = null;
    }
}
