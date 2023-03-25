package net.buildtheearth.terraplusplus.projection.wkt.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTProjection extends WKTObject.WithNameAndID {
    @NonNull
    private final WKTProjectionMethod method;

    @NonNull
    @Builder.Default
    private final List<WKTProjectionParameter> parameters = Collections.emptyList();

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("CONVERSION")
                .writeQuotedLatinString(this.name())
                .writeRequiredObject(this.method)
                .writeObjectList(this.parameters)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
