package net.buildtheearth.terraplusplus.util.geo;

public class LatLng {
    private final Double lat;
    private final Double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng() {
        lat = null;
        lng = null;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
