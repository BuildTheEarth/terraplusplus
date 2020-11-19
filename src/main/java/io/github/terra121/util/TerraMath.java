package io.github.terra121.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TerraMath {
    public double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
