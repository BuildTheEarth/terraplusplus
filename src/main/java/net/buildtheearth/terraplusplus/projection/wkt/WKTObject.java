package net.buildtheearth.terraplusplus.projection.wkt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTProjectedCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTStaticGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDynamicGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTGeodeticDatumEnsemble;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTStaticGeodeticDatum;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTID;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTScaleUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public interface WKTObject {
    String toPrettyString();

    String toString(@NonNull WKTStyle style);

    void write(@NonNull WKTWriter writer) throws IOException;

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

            //@JsonSubTypes.Type(value = WKTUnit.class, name = "Unit"),
            @JsonSubTypes.Type(value = WKTAngleUnit.class, name = "AngularUnit"),
            @JsonSubTypes.Type(value = WKTLengthUnit.class, name = "LinearUnit"),
            @JsonSubTypes.Type(value = WKTScaleUnit.class, name = "ScaleUnit"),
    })
    interface AutoDeserialize extends WKTObject {
        default <T extends WKTObject> T asWKTObject() {
            return uncheckedCast(this);
        }
    }

    /**
     * @author DaPorkchop_
     */
    interface WithID {
        WKTID id();

        default List<WKTID> ids() { //TODO: support multiple IDs
            return Collections.singletonList(this.id());
        }
    }

    /**
     * @author DaPorkchop_
     */
    interface WithName {
        String name();
    }

    /**
     * @author DaPorkchop_
     */
    interface WithScopeExtentIdentifierRemark extends WithID { //TODO: marker interface
    }
}
