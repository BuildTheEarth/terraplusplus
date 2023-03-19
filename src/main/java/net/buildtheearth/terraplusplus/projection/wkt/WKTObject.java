package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class WKTObject {
    protected static final WKTParseSchema<WKTObject> BASE_PARSE_SCHEMA = WKTParseSchema.<WKTObject, WKTObjectBuilder<WKTObject, ?>>builder(() -> null, WKTObjectBuilder::build)
            .permitKeyword("")
            .optionalObjectProperty(WKTID.PARSE_SCHEMA, WKTObjectBuilder::id)
            .build();

    @Builder.Default
    private final WKTID id = null;

    @Override
    @SneakyThrows(IOException.class)
    public String toString() {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, " ", "", " ")) {
            this.write(writer);
        }
        return builder.toString();
    }

    public abstract void write(@NonNull WKTWriter writer) throws IOException;
}
