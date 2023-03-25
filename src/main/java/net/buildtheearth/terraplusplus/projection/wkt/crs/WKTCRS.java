package net.buildtheearth.terraplusplus.projection.wkt.crs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTBoundingBox;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTCRS extends WKTObject.WithNameAndID implements WKTObject.AutoDeserialize, WKTObject.ScopeExtentIdentifierRemark {
    @Builder.Default
    private final String scope = null;

    @Builder.Default
    private final String area = null;

    @Builder.Default
    private final String usage = null;

    @Builder.Default
    private final WKTBoundingBox bbox = null;
}