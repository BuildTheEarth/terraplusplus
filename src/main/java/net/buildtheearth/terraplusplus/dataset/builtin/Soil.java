package net.buildtheearth.terraplusplus.dataset.builtin;

import LZMA.LzmaInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.buildtheearth.terraplusplus.util.RLEByteArray;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

import java.io.InputStream;

import static net.daporkchop.lib.common.math.PMath.*;

public class Soil extends AbstractBuiltinDataset {
    protected static final int COLS = 10800;
    protected static final int ROWS = 5400;

    private static final Cached<RLEByteArray> DATA_CACHE = Cached.global((IOSupplier<RLEByteArray>) () -> {
        ByteBuf buf;
        try (InputStream in = new LzmaInputStream(Climate.class.getResourceAsStream("soil.lzma"))) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        RLEByteArray.Builder builder = RLEByteArray.builder();
        for (int i = 0, lim = buf.readableBytes(); i < lim; i++) {
            builder.append(buf.getByte(i));
        }
        return builder.build();
    }, ReferenceStrength.SOFT);

    private final RLEByteArray data = DATA_CACHE.get();

    public Soil() {
        super(COLS, ROWS);
    }

    @Override
    protected double get(double fx, double fy) {
        int x = floorI(fx);
        int y = floorI(fy);
        if (x >= COLS || x < 0 || y >= ROWS || y < 0) {
            return 0;
        }
        return this.data.get(y * COLS + x);
    }
}