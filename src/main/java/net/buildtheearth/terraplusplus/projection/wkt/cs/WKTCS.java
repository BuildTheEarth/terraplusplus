package net.buildtheearth.terraplusplus.projection.wkt.cs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;

import java.io.IOException;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTCS extends WKTObject.WithID {
    @NonNull
    @JsonProperty("subtype")
    private final Type type;

    @NonNull
    @JsonProperty("axis")
    private final List<WKTAxis> axes;

    @Builder.Default
    private final WKTUnit unit = null;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("CS")
                .writeEnum(this.type)
                .writeUnsignedNumericLiteral(this.axes.size())
                .writeOptionalObject(this.id())
                .endObject()
                .writeObjectList(this.axes)
                .writeOptionalObject(this.unit);
    }

    /**
     * @author DaPorkchop_
     */
    public enum Type {
        affine,
        Cartesian,
        cylindrical,
        ellipsoidal,
        linear,
        parametric,
        polar,
        spherical,
        vertical,
    }
}
