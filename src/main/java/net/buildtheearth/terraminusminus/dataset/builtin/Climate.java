package net.buildtheearth.terraminusminus.dataset.builtin;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.buildtheearth.terraminusminus.util.IntToDoubleBiFunction;
import net.buildtheearth.terraminusminus.dataset.BlendMode;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

import static net.daporkchop.lib.common.util.PValidation.*;

public abstract class Climate extends AbstractBuiltinDataset implements IntToDoubleBiFunction {
    public static final int COLS = 720;
    public static final int ROWS = 360;

    private static final String RESOURCE_PATH = "climate.gz";

    private static final Cached<double[]> DATA_CACHE = Cached.global((IOSupplier<double[]>) () -> {
        ByteBuf buf;
        try (InputStream compressedStream = Climate.class.getResourceAsStream(RESOURCE_PATH)) {
            checkState(compressedStream != null, "Missing internal dataset resource: " + RESOURCE_PATH);
            InputStream stream = new GZIPInputStream(compressedStream);
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(stream));
        }

        double[] out = new double[ROWS * COLS * 2];
        for (int i = 0; i < out.length; ) {
            out[i++] = buf.readFloat();
            out[i++] = buf.readFloat();
        }
        return out;
    }, ReferenceStrength.SOFT);

    protected final double[] data = DATA_CACHE.get();

    public Climate() {
        super(COLS, ROWS);
    }

    @Override
    protected double get(double x, double y) {
        return BlendMode.LINEAR.get(x, y, this);
    }

    public static class Precipitation extends Climate {
        @Override
        public double apply(int x, int y) {
            if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
                return -50.0d;
            }
            return this.data[(x * ROWS + y) << 1];
        }
    }

    public static class Temperature extends Climate {
        @Override
        public double apply(int x, int y) {
            if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
                return -50.0d;
            }
            return this.data[(x * ROWS + y) << 1];
        }
    }
}