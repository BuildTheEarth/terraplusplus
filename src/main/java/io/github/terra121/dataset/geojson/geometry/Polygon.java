package io.github.terra121.dataset.geojson.geometry;

import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;
import lombok.NonNull;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Data
public final class Polygon implements Geometry {
    protected final LineString outerRing;
    protected final LineString[] innerRings;

    public Polygon(@NonNull LineString outerRing, @NonNull LineString[] innerRings) {
        checkArg(outerRing.isLinearRing(), "outerRing is not a linear ring!");
        for (int i = 0; i < innerRings.length; i++) {
            checkArg(innerRings[i].isLinearRing(), "innerRings[%d] is not a linear ring!", i);
        }
        this.outerRing = outerRing;
        this.innerRings = innerRings;
    }
}
