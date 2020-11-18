package io.github.terra121.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TerraMath {
    public double lerp(int a, int b, int t) {
        return a + (b - a) * t;
    }
}
