package net.buildtheearth.terraplusplus.crs;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.TerraUtils;

/**
 * @author DaPorkchop_
 */
@Data
@With(AccessLevel.PRIVATE)
public final class CompoundCRS implements CRS {
    @NonNull
    private final ImmutableList<SingleCRS> includes;

    @Override
    public CompoundCRS intern() {
        return InternHelper.intern(this.withIncludes(TerraUtils.internElements(this.includes)));
    }
}
