package net.buildtheearth.terraplusplus.projection.wkt.unit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(using = WKTUnit.UnitDeserializer.class)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTUnit extends AbstractWKTObject.WithID implements WKTObject.AutoDeserialize {
    protected static final WKTParseSchema<WKTUnit> BASE_PARSE_SCHEMA = WKTParseSchema.<WKTUnit, WKTUnitBuilder<WKTUnit, ?>>builder(() -> null, WKTUnitBuilder::build)
            .permitKeyword("")
            .requiredStringProperty(WKTUnitBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTUnitBuilder::conversionFactor)
            .inheritFrom(AbstractWKTObject.WithID.BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    /**
     * The number of base units per unit.
     */
    @JsonProperty("conversion_factor")
    private final double conversionFactor;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("UNIT")
                .writeQuotedLatinString(this.name())
                .writeUnsignedNumericLiteral(this.conversionFactor())
                .writeOptionalObject(this.id())
                .endObject();
    }

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
            return ctxt.readValue(p, AutoDeserialize.class).asWKTObject();
        }
    }
}
