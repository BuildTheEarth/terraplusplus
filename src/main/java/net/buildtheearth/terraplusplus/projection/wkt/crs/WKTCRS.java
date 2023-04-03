package net.buildtheearth.terraplusplus.projection.wkt.crs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTCRS extends AbstractWKTObject.WithNameAndScopeExtentIdentifierRemark implements AbstractWKTObject.AutoDeserialize {
}
