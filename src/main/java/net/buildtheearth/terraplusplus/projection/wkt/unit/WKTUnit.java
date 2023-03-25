package net.buildtheearth.terraplusplus.projection.wkt.unit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(using = WKTUnit.UnitDeserializer.class)
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

    protected static final class UnitDeserializer extends JsonDeserializer<WKTUnit> {
        @Override
        public WKTUnit deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken token = p.currentToken();
            if (token == JsonToken.VALUE_STRING) {
                String text = p.getText();
                switch (text) {
                    case "degree":
                        return WKTAngleUnit.DEGREE;
                    case "metre":
                        return WKTLengthUnit.METRE;
                    case "unity":
                        return WKTScaleUnit.UNITY;
                    default:
                        throw new IllegalArgumentException("unexpected text: " + text);
                }
            }
            throw new UnsupportedOperationException(); //TODO
        }
    }
}
