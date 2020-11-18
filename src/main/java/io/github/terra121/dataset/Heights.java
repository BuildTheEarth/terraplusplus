package io.github.terra121.dataset;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.EarthTerrainProcessor;
import io.github.terra121.TerraConfig;
import io.github.terra121.projection.MapsProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;

import javax.imageio.ImageIO;

public class Heights extends DoubleTiledDataset {
    private final Water water;
    private final int zoom;

    public Heights(int zoom, boolean smooth, Water water) {
        super(256, TerraConfig.cacheSize, new MapsProjection(), 1 << (zoom + 8), smooth);
        this.zoom = zoom;
        this.water = water;
    }

    public Heights(int zoom, Water water) {
        this(zoom, false, water);
    }

    @Override
    protected String[] urls() {
        return TerraConfig.data.heights;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("tile.zoom", String.valueOf(this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        ImageIO.read(new ByteBufInputStream(data)).getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];
        //TODO: process data (see getOfficialHeight below)
        return out;
    }

    /*@Override
    protected double getOfficialHeight(Coord coord, boolean lidar) {
        double ret = super.getOfficialHeight(coord, lidar);

        //shoreline smoothing
        if (this.water != null && ret > -1 && ret != 0 && ret < 200) {
            double[] proj = this.projection.toGeo(coord.x / this.scaleX, coord.y / this.scaleY); //another projection, i know (horrendous)
            double mine = this.water.estimateLocal(proj[0], proj[1]);

            double oceanRadius = 2.0 / (60 * 60);
            if (mine > 1.4 || (ret > 10 & (mine > 1 ||
                                           this.water.estimateLocal(proj[0] + oceanRadius, proj[1]) > 1 || this.water.estimateLocal(proj[0] - oceanRadius, proj[1]) > 1 ||
                                           this.water.estimateLocal(proj[0], proj[1] + oceanRadius) > 1 || this.water.estimateLocal(proj[0], proj[1] - oceanRadius) > 1))) {
                return -1;
            }
        }
        return ret;
    }

    @Override
    protected double dataToDouble(int data) {
        if (data >> 24 != 0) { //check for alpha value
            data = (data & 0x00ffffff) - 8388608;
            if (this.zoom > 10 && data < -1500 * 256) {
                data = 0;
            }
            return data / 256.0;
        }
        return -10000000;
    }*/
}
