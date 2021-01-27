package io.github.terra121.dataset.impl;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.util.IntToDoubleBiFunction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.ToString;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;

import java.io.InputStream;

public class Climate {
    public static final int COLS = 720;
    public static final int ROWS = 360;

    private static final Ref<double[]> DATA_CACHE = Ref.soft((IOSupplier<double[]>) () -> {
        ByteBuf buf;
        try (InputStream in = Climate.class.getResourceAsStream("/assets/terra121/data/climate.dat")) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        double[] out = new double[ROWS * COLS * 2];
        for (int i = 0; i < out.length; ) {
            buf.skipBytes(8);
            out[i++] = buf.readFloat();
            out[i++] = buf.readFloat();
        }
        return out;
    });

    private final double[] data = DATA_CACHE.get();

    private final IntToDoubleBiFunction getOfficialTemp = (x, y) -> {
        if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
            return -50.0d;
        }
        return this.data[(x * ROWS + y) << 1];
    };
    private final IntToDoubleBiFunction getOfficialPrecip = (x, y) -> {
        if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
            return 0.0d;
        }
        return this.data[((x * ROWS + y) << 1) + 1];
    };

    public ClimateData getPoint(double x, double y) {
        x = (COLS * (x + 180) / 360);
        y = (ROWS * (90 - y) / 180);
        return new ClimateData(BlendMode.LINEAR.get(x, y, this.getOfficialTemp), BlendMode.LINEAR.get(x, y, this.getOfficialPrecip));
    }

    //rough estimate of snow cover
    public boolean isSnow(double x, double y, double alt) {
        return alt > 5000 || this.getPoint(x, y).temp < 0; //high elevations or freezing temperatures
    }

    @AllArgsConstructor
    @ToString
    public static final class ClimateData {
        public final double temp;
        public final double precip;
    }
}