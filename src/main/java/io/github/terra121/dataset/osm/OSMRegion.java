package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.osm.poly.Polygon;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.util.bvh.BVH;
import net.minecraft.util.math.ChunkPos;

public class OSMRegion {
    public ChunkPos coord;
    public double south;
    public double west;

    public BVH<Segment> segments;
    public BVH<Polygon> polygons;

    public OSMRegion(ChunkPos coord) {
        this.coord = coord;

        this.south = coord.z * OpenStreetMap.TILE_SIZE;
        this.west = coord.x * OpenStreetMap.TILE_SIZE;
    }

    public int hashCode() {
        return this.coord.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof OSMRegion) && this.coord.equals(((OSMRegion) other).coord);
    }
}
