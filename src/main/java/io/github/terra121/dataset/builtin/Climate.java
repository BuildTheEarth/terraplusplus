package io.github.terra121.dataset.builtin;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.util.IntToDoubleBiFunction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.ToString;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.InputStream;

public abstract class Climate extends AbstractBuiltinDataset implements IntToDoubleBiFunction {
    public static final int COLS = 720;
    public static final int ROWS = 360;

    private static final Ref<double[]> DATA_CACHE = Ref.soft((IOSupplier<double[]>) () -> {
        ByteBuf buf;
        try (InputStream in = new BZip2CompressorInputStream(Climate.class.getResourceAsStream("climate.bz2"))) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        double[] out = new double[ROWS * COLS * 2];
        for (int i = 0; i < out.length; ) {
            out[i++] = buf.readFloat();
            out[i++] = buf.readFloat();
        }
        return out;
    });

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