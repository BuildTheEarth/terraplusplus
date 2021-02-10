package net.buildtheearth.terraplusplus.util;

import lombok.Getter;

@Getter
public enum CardinalDirection {
    N(337.5, 22.5, "North"), S(157.5, 202.5, "South"), E(67.5, 112.5, "East"), W(247.5, 292.5, "West"),
    NW(292.5, 337.5, "Northwest"), SW(202.5, 247.5, "Southwest"), SE(112.5, 157.5, "Southeast"),
    NE(22.5, 67.5, "Northeast"), UNKNOWN(360.5, 10000000.0, "Unknown");

    private double min;

    private double max;

    private String realName;

    private String translationKey;

    private CardinalDirection(double min, double max, String realName) {
        this.min = min;
        this.max = max;
        this.realName = realName;
        this.translationKey = "terra121.cardinal_directions." + this.name().toLowerCase();
    }

    /**
     * @param azimuth - an azimuth
     * @return the CardinalDirection the given azimuth faces
     */
    public static CardinalDirection azimuthToFacing(float azimuth) {
        for (CardinalDirection facingToBeTestedFor : CardinalDirection.values()) {
            if (facingToBeTestedFor == CardinalDirection.N) {

                if (azimuth >= facingToBeTestedFor.min() && azimuth <= 360) {
                    return facingToBeTestedFor;

                } else if (azimuth >= 0 && azimuth <= facingToBeTestedFor.max()) {
                    return facingToBeTestedFor;
                }

            } else if (azimuth >= facingToBeTestedFor.min() && azimuth <= facingToBeTestedFor.max()) {
                return facingToBeTestedFor;
            }
        }
        return CardinalDirection.UNKNOWN;
    }

    /**
     * @param azimuth - an azimuth
     * @return true if the given azimuth faces this cardinal direction
     */
    public boolean matches(float azimuth) {
        return azimuthToFacing(azimuth) == this;
    }

}
