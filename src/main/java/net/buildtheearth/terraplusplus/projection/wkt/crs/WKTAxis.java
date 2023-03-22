package net.buildtheearth.terraplusplus.projection.wkt.crs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTAxis extends WKTObject {
    public static final WKTParseSchema<WKTAxis> PARSE_SCHEMA = WKTParseSchema.builder(WKTAxisBuilderImpl::new, WKTAxisBuilder::build)
            .permitKeyword("AXIS")
            .requiredStringProperty(WKTAxisBuilder::name)
            .requiredEnumProperty(Direction.class, WKTAxisBuilder::direction)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final Direction direction;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("AXIS")
                .writeQuotedLatinString(this.name)
                .writeEnum(this.direction)
                .endObject();
    }

    /**
     * @author DaPorkchop_
     */
    public enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        UP,
        DOWN,
        OTHER,
    }
}
