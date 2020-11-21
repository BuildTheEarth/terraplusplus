package io.github.terra121.dataset.osm.segment;

import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.OpenStreetMap;
import lombok.ToString;

import static java.lang.Math.*;

/**
 * @author DaPorkchop_
 */
@ToString(exclude = "region")
public class Segment {
    public SegmentType type;
    public double lat0;
    public double lon0;
    public double lat1;
    public double lon1;
    public OpenStreetMap.Attributes attribute;
    public byte layer_number;
    public double slope;
    public double offset;

    public byte lanes;

    OSMRegion region;

    public Segment(double lon0, double lat0, double lon1, double lat1, SegmentType type, byte lanes, OSMRegion region, OpenStreetMap.Attributes att, byte ly) {
        //slope must not be infinity, slight inaccuracy shouldn't even be noticible unless you go looking for it
        double dif = lon1 - lon0;
        if (abs(dif) < 0.01d) {
            lon1 += copySign(0.01d, dif);
        }

        this.lat0 = lat0;
        this.lon0 = lon0;
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.type = type;
        this.attribute = att;
        this.lanes = lanes;
        this.region = region;
        this.layer_number = ly;

        this.slope = (lat1 - lat0) / (lon1 - lon0);
        this.offset = lat0 - this.slope * lon0;
    }

    @Override
    public int hashCode() {
        return (int) ((this.lon0 * 79399) + (this.lat0 * 100000) + (this.lat1 * 13467) + (this.lon1 * 103466));
    }

    @Override
    public boolean equals(Object o) {
        Segment e = (Segment) o;
        return e.lat0 == this.lat0 && e.lon0 == this.lon0 && e.lat1 == this.lat1 && e.lon1 == e.lon1;
    }
}
