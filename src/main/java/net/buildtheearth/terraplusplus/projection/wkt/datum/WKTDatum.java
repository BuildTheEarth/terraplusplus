package net.buildtheearth.terraplusplus.projection.wkt.datum;

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
public abstract class WKTDatum extends AbstractWKTObject.WithNameAndID implements AbstractWKTObject.AutoDeserialize {
}
