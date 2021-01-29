package io.github.terra121.dataset.builtin;

import io.github.terra121.util.RLEByteArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.InputStream;

public class Soil {
    public static final int COLS = 10800;
    public static final int ROWS = 5400;

    private static final Ref<RLEByteArray> DATA_CACHE = Ref.soft((IOSupplier<RLEByteArray>) () -> {
        ByteBuf buf;
        try (InputStream in = new BZip2CompressorInputStream(Climate.class.getResourceAsStream("soil.bz2"))) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        RLEByteArray.Builder builder = RLEByteArray.builder();
        for (int i = 0, lim = buf.readableBytes(); i < lim; i++) {
            builder.append(buf.getByte(i));
        }
        return builder.build();
    });

    private final RLEByteArray data = DATA_CACHE.get();

    public byte getOfficial(int x, int y) {
        if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
            return 0;
        }
        return this.data.get(x + y * COLS);
    }

    public byte getPoint(double x, double y) {
        int X = (int) (COLS * (x + 180) / 360);
        int Y = (int) (ROWS * (90 - y) / 180);

        return this.getOfficial(X, Y);
    }
}