package io.github.terra121.util;

public enum CardinalDirection {
	N (337.5, 22.5, "North", "terra121.cardinal_directions.north"),
	S (157.5, 202.5, "South", "terra121.cardinal_directions.south"),
	E (67.5, 112.5, "East", "terra121.cardinal_directions.east"),
	W (247.5, 292.5, "West", "terra121.cardinal_directions.west"),
	NW (292.5, 337.5, "Northwest", "terra121.cardinal_directions.northwest"),
	SW (202.5, 247.5, "Southwest", "terra121.cardinal_directions.southwest"),
	SE (112.5, 157.5, "Southeast", "terra121.cardinal_directions.southeast"),
	NE (22.5, 67.5, "Northeast", "terra121.cardinal_directions.northeast"),
	UNKNOWN (360.5, 10000000.0, "Unknown", "terra121.cardinal_directions.north");
	
	private final double min;
	
	private final double max;
	
	private final String realName;
	
	private final String translationKey;
	
	private CardinalDirection(double min, double max, String realName, String translationKey) {
		this.min = min;
		this.max = max;
		this.realName = realName;
		this.translationKey = translationKey;
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
	public String getTranslationKey() {
		return translationKey;
	}
	/**
	 * @param azimuth - an azimuth
	 * @return true if the given azimuth faces this cardinal direction
	 */
	public boolean matches(float azimuth) {
		return azimuthToFacing(azimuth) == this;
	}
	/**
	 * @param azimuth - an azimuth
	 * @return the CardinalDirection the given azimuth faces
	 */
	public static CardinalDirection azimuthToFacing(float azimuth) {
		for (CardinalDirection facingToBeTestedFor : CardinalDirection.values()) {
			if (facingToBeTestedFor == CardinalDirection.N) {
				
				if (azimuth >= facingToBeTestedFor.getMin() && azimuth <= 360) {
					return facingToBeTestedFor;
					
				} else if (azimuth >= 0 && azimuth <= facingToBeTestedFor.getMax()) {
					return facingToBeTestedFor;
				}
				
			} else if (azimuth >= facingToBeTestedFor.getMin() && azimuth <= facingToBeTestedFor.getMax()) {
				return facingToBeTestedFor;
			}
		}
		return CardinalDirection.UNKNOWN;
	}
}
