package net.buildtheearth.terraplusplus.projection.wkt.unit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(using = WKTValueInDegreeOrValueAndUnit.AutoDeserializer.class)
@JsonIgnoreProperties("$schema")
@EqualsAndHashCode(callSuper = false)
@Getter
public final class WKTValueInDegreeOrValueAndUnit {
    @NonNull
    private final Number value;

    private final WKTAngleUnit unit;

    public WKTValueInDegreeOrValueAndUnit(@NonNull Number value) {
        this.value = value;
        this.unit = null;
    }

    public WKTValueInDegreeOrValueAndUnit(
            @JsonProperty("value") @NonNull Number value,
            @JsonProperty("unit") @NonNull WKTAngleUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    protected static final class AutoDeserializer extends JsonDeserializer<WKTValueInDegreeOrValueAndUnit> {
        @Override
        public WKTValueInDegreeOrValueAndUnit deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (p.currentToken() == JsonToken.START_OBJECT) {
                Tmp tmp = ctxt.readValue(p, Tmp.class);
                return new WKTValueInDegreeOrValueAndUnit(tmp.value, (WKTAngleUnit) tmp.unit);
            } else {
                return new WKTValueInDegreeOrValueAndUnit(p.getNumberValue());
            }
        }

        @Jacksonized
        @Builder
        private static class Tmp {
            @NonNull
            final Number value;

            @NonNull
            final WKTUnit unit;
        }
    }
}
