package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class WKTObject {
    @Builder.Default
    private final WKTID id = null;
}
