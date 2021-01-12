package io.github.terra121.util;
public enum CardinalDirection {
	N (337.5, 22.5, "North"),
	S (157.5, 202.5, "South"),
	E (67.5, 112.5, "East"),
	W (247.5, 292.5, "West"),
	NW (292.5, 337.5, "Northwest"),
	SW (202.5, 247.5, "Southwest"),
	SE (112.5, 157.5, "Southeast"),
	NE (22.5, 67.5, "Northeast"),
	UNKNOWN (360.5, 10000000.0, "Unknown");
    private final double min;
    private final double max;
    private final String realName;
    private CardinalDirection(double min, double max, String realName) {
        this.min = min;
        this.max = max;
        this.realName = realName;
    }
    public double getMin() {
    	return min;
    }
    public double getMax() {
    	return max;
    }
    public String getRealName() {
    	return realName;
    }
}
