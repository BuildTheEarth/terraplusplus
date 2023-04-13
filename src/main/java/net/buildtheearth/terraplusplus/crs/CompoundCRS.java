package net.buildtheearth.terraplusplus.crs;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@Data
public final class CompoundCRS implements CRS {
    @NonNull
    private final ImmutableList<SingleCRS> includes;
}
