package net.buildtheearth.terraplusplus.projection.wkt.unit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTUnit extends WKTObject.WithID {
    protected static final WKTParseSchema<WKTUnit> BASE_PARSE_SCHEMA = WKTParseSchema.<WKTUnit, WKTUnitBuilder<WKTUnit, ?>>builder(() -> null, WKTUnitBuilder::build)
            .permitKeyword("")
            .requiredStringProperty(WKTUnitBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTUnitBuilder::conversionFactor)
            .inheritFrom(WKTObject.WithID.BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    /**
     * The number of base units per unit.
     */
    private final double conversionFactor;
}
