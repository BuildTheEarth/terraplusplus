package io.github.terra121.util;

public class AzimuthUtils {
	public static String azimuthToFacing(float azimuth) {
		if (azimuth > 270 && azimuth < 360) {
			return "Northwest";
		}
		else if (azimuth > 180 && azimuth < 270) {
			return "Southwest";
		}
		else if (azimuth > 90 && azimuth < 180) {
			return "Southeast";
		}
		else if (azimuth > 0 && azimuth < 90) {
			return "Northeast";
		}
		else if (azimuth == 0) {
			return "North";
		}
		else if (azimuth == 270) {
			return "West";
		}
		else if (azimuth == 180) {
			return "South";
		}
		else if (azimuth == 90) {
			return "East";
		}
		else {
			return "Invalid";
		}
	}
	

}
