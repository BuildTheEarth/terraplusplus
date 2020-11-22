package io.github.terra121.dataset;

import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.projection.MapsProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;

import javax.imageio.ImageIO;

public class Heights extends DoubleTiledDataset {
    protected final Water water;
    private final int zoom;

    public Heights(Water water, int zoom, boolean smooth) {
        super(new MapsProjection(), 1 << (zoom + 8), smooth);
        this.water = water;
        this.zoom = zoom;
    }

    @Override
    protected String[] urls() {
        return TerraConfig.data.heights;
    }

    @Override
    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        super.addProperties(tileX, tileZ, builder);

        builder.put("zoom", String.valueOf(this.zoom));
    }

    @Override
    protected double[] decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception {
        int[] iData = new int[TILE_SIZE * TILE_SIZE];
        ImageIO.read(new ByteBufInputStream(data)).getRGB(0, 0, TILE_SIZE, TILE_SIZE, iData, 0, TILE_SIZE);

        double[] out = new double[TILE_SIZE * TILE_SIZE];
        for (int i = 0; i < iData.length; i++) {
            out[i] = ((iData[i] & 0x00FFFFFF) - 0x800000) / 256.0d;
        }

        if (this.water != null) {
            for (int z = 0; z < TILE_SIZE; z++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    double height = out[z * TILE_SIZE + x];
                    if (height > -1.0d && height != 0.0d && height < 200.0d) {
                        double[] proj = this.projection.toGeo((tileX * TILE_SIZE + x) / this.scale, (tileZ * TILE_SIZE + z) / this.scale);
                        double lon = proj[0];
                        double lat = proj[1];

                        double mine = this.water.estimateLocal(lon, lat);

                        double oceanRadius = 2.0d / (60.0d * 60.0d);
                        if (mine > 1.4d || (height > 10.0d & (mine > 1.0d
                                                              || this.water.estimateLocal(lon + oceanRadius, lat) > 1.0d
                                                              || this.water.estimateLocal(lon - oceanRadius, lat) > 1.0d
                                                              || this.water.estimateLocal(lon, lat + oceanRadius) > 1.0d
                                                              || this.water.estimateLocal(lon, lat - oceanRadius) > 1.0d))) {
                            height = -1.0d;
                        }
                    }
                    out[z * TILE_SIZE + x] = height;
                }
            }
        }

        return out;
    }
}
