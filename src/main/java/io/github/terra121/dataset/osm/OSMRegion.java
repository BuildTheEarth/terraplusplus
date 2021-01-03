package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.impl.LandLine;
import io.github.terra121.dataset.impl.Water;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.util.bvh.BVH;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public class OSMRegion {
    public ChunkPos coord;
    public Water water;
    public LandLine southLine;
    public LandLine[] lines;
    public double south;
    public double west;

    public short[][] indexes;
    public byte[][] states;

    public final BVH<OSMSegment> segments;
    public final BVH<OSMPolygon> polygons;

    public OSMRegion(ChunkPos coord, Water water, @NonNull BVH<OSMSegment> segments, @NonNull BVH<OSMPolygon> polygons) {
        this.coord = coord;
        this.water = water;

        this.segments = segments;
        this.polygons = polygons;

        this.lines = new LandLine[Water.TILE_SIZE];
        for (int i = 0; i < this.lines.length; i++) {
            this.lines[i] = new LandLine();
        }

        this.southLine = new LandLine();

        this.south = coord.z * OpenStreetMap.TILE_SIZE;
        this.west = coord.x * OpenStreetMap.TILE_SIZE;
    }

    public void addWaterEdge(double slon, double slat, double elon, double elat, long type) {

        slat -= this.south;
        elat -= this.south;
        slon -= this.west;
        elon -= this.west;

        slon /= OpenStreetMap.TILE_SIZE / Water.TILE_SIZE;
        elon /= OpenStreetMap.TILE_SIZE / Water.TILE_SIZE;

        slat /= OpenStreetMap.TILE_SIZE / Water.TILE_SIZE;
        elat /= OpenStreetMap.TILE_SIZE / Water.TILE_SIZE;

        if (slat <= 0 || elat <= 0 && (slat >= 0 || elat >= 0)) {
            if (slat == 0) {
                slat = 0.00000001;
            }
            if (elat == 0) {
                elat = 0.00000001;
            }

            if (elat != slat) {
                double islope = (elon - slon) / (elat - slat);
                this.southLine.add(elon - islope * elat, type);
            }
        }

        if (slon != elon) {
            double slope = (elat - slat) / (elon - slon);

            int beg = (int) Math.ceil(Math.min(slon, elon));
            int end = (int) Math.floor(Math.max(slon, elon));

            if (beg < 0) {
                beg = 0;
            }
            if (end >= Water.TILE_SIZE) {
                end = Water.TILE_SIZE - 1;
            }

            for (int x = beg; x <= end; x++) {
                this.lines[x].add(slope * x + (elat - slope * elon), type);
            }
        }
    }

    private void addComp(Object[] line, int x) {
        this.indexes[x] = (short[]) line[0];
        this.states[x] = (byte[]) line[1];
    }

    public void renderWater(Set<Long> ground) {
        this.indexes = new short[Water.TILE_SIZE][];
        this.states = new byte[Water.TILE_SIZE][];

        this.southLine.run(Water.TILE_SIZE, ground, (status, x) -> this.addComp(this.lines[x].compileBreaks(new HashSet<>(status), Water.TILE_SIZE), x));

        //we are done with these resources, now that they are compiled
        this.lines = null;
        this.southLine = null;
    }

    //another fliping binary search, why can't java have a decent fuzzy one built in
    public int getStateIdx(short x, short y) {
        short[] index = this.indexes[x];

        int min = 0;
        int max = index.length;

        while (min < max - 1) {
            int mid = min + (max - min) / 2;
            if (index[mid] < y) {
                min = mid;
            } else if (index[mid] > y) {
                max = mid;
            } else {
                return mid;
            }
        }
        return min;
    }

    @Override
    public int hashCode() {
        return this.coord.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof OSMRegion) && this.coord.equals(((OSMRegion) other).coord);
    }
}
