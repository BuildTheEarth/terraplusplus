package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTCRS extends AbstractWKTObject.WithNameAndScopeExtentIdentifierRemark implements AbstractWKTObject.AutoDeserialize {
    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public abstract static class WithCoordinateSystem extends WKTCRS {
        @NonNull
        @JsonProperty("coordinate_system")
        private final WKTCS coordinateSystem;
    }
}
