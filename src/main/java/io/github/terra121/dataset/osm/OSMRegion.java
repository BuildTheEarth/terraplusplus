package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.util.bvh.BVH;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

public class OSMRegion {
    public ChunkPos coord;
    public double south;
    public double west;

    public final BVH<OSMSegment> segments;
    public final BVH<OSMPolygon> polygons;

    public OSMRegion(ChunkPos coord, @NonNull BVH<OSMSegment> segments, @NonNull BVH<OSMPolygon> polygons) {
        this.coord = coord;

        this.segments = segments;
        this.polygons = polygons;

        this.south = coord.z * OpenStreetMap.TILE_SIZE;
        this.west = coord.x * OpenStreetMap.TILE_SIZE;
    }

    public int hashCode() {
        return this.coord.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof OSMRegion) && this.coord.equals(((OSMRegion) other).coord);
    }
}
