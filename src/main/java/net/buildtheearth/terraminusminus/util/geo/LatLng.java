package net.buildtheearth.terraminusminus.util.geo;

public class LatLng {
    private final Double lat;
    private final Double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng() {
        this.lat = null;
        this.lng = null;
    }

    public Double getLat() {
        return this.lat;
    }

    public Double getLng() {
        return this.lng;
    }
}
