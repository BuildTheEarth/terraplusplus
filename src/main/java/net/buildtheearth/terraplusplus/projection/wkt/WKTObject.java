package net.buildtheearth.terraplusplus.projection.wkt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTProjectedCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTStaticGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDynamicGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTGeodeticDatumEnsemble;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTStaticGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTID;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class WKTObject {
    @Override
    public String toString() {
        return this.toString(WKTStyle.ONE_LINE);
    }

    public String toPrettyString() {
        return this.toString(WKTStyle.PRETTY);
    }

    @SneakyThrows(IOException.class)
    public String toString(@NonNull WKTStyle style) {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, style)) {
            this.write(writer);
        }
        return builder.toString();
    }

    public abstract void write(@NonNull WKTWriter writer) throws IOException;

    /**
     * @author DaPorkchop_
     */
    @JsonIgnoreProperties("$schema")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = WKTProjectedCRS.class, name = "ProjectedCRS"),
            @JsonSubTypes.Type(value = WKTStaticGeographicCRS.class, name = "GeodeticCRS"),
            @JsonSubTypes.Type(value = WKTStaticGeographicCRS.class, name = "GeographicCRS"),

            @JsonSubTypes.Type(value = WKTGeodeticDatumEnsemble.class, name = "DatumEnsemble"),
            @JsonSubTypes.Type(value = WKTDynamicGeodeticDatum.class, name = "DynamicGeodeticReferenceFrame"),
            @JsonSubTypes.Type(value = WKTStaticGeodeticDatum.class, name = "GeodeticReferenceFrame"),

            @JsonSubTypes.Type(value = WKTEllipsoid.class, name = "Ellipsoid"),
    })
    public interface AutoDeserialize {
        default <T extends WKTObject> T asWKTObject() {
            return uncheckedCast(this);
        }
    }

    public interface ScopeExtentIdentifierRemark { //TODO: marker interface
    }

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

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithNameAndID extends WKTObject.WithID {
        @NonNull
        private final String name;
    }
}
