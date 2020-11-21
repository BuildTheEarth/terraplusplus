package io.github.terra121.dataset;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class HeightsWaterMix implements ScalarDataset {
    @NonNull
    protected final Heights heights;
    @NonNull
    protected final Water water;

    @Override
    public double estimateLocal(double lon, double lat) {
        double height = this.heights.estimateLocal(lon, lat);
        if (height > -1.0d && height != 0.0d && height < 200.0d) {
            double mine = this.water.estimateLocal(lon, lat);

            double oceanRadius = 2.0d / (60.0d * 60.0d);
            if (mine > 1.4d || (height > 10.0d & (mine > 1.0d
                                                  || this.water.estimateLocal(lon + oceanRadius, lat) > 1.0d
                                                  || this.water.estimateLocal(lon - oceanRadius, lat) > 1.0d
                                                  || this.water.estimateLocal(lon, lat + oceanRadius) > 1.0d
                                                  || this.water.estimateLocal(lon, lat - oceanRadius) > 1.0d))) {
                return -1.0d;
            }
        }
        return height;
    }
}
