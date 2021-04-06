package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Helper class for transformations on {@code double[]} tiles.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class TileFormatUtils {
    public void swapAxes(@NonNull double[] arr, int resolution) {
        for (int i = 1; i < resolution; i++) {
            for (int j = 0; j < i; j++) {
                int a = i * resolution + j;
                int b = j * resolution + i;
                double t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
    }

    public void flipX(@NonNull double[] arr, int resolution) {
        for (int z = 0; z < resolution; z++) {
            for (int x = 0, lim = resolution >> 1; x < lim; x++) {
                int a = z * resolution + x;
                int b = z * resolution + (resolution - x - 1);
                double t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
    }

    public void flipZ(@NonNull double[] arr, int resolution) {
        for (int z = 0, lim = resolution >> 1; z < lim; z++) {
            for (int x = 0; x < resolution; x++) {
                int a = z * resolution + x;
                int b = (resolution - z - 1) * resolution + x;
                double t = arr[a];
                arr[a] = arr[b];
                arr[b] = t;
            }
        }
    }
}
