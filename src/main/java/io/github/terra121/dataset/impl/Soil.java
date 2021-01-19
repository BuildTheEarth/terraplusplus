package io.github.terra121.dataset.impl;

import io.github.terra121.util.RandomAccessRunlength;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;

import java.io.InputStream;

public class Soil {
    public static final int COLS = 10800;
    public static final int ROWS = 5400;

    private static final Ref<RandomAccessRunlength<Byte>> DATA_CACHE = Ref.soft((IOSupplier<RandomAccessRunlength<Byte>>) () -> {
        ByteBuf buf;
        try (InputStream in = Climate.class.getResourceAsStream("/assets/terra121/data/suborder.img")) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        RandomAccessRunlength<Byte> data = new RandomAccessRunlength<>();
        while (buf.isReadable()) {
            data.add(buf.readByte());
        }
        return data;
    });

    private final RandomAccessRunlength<Byte> data = DATA_CACHE.get();

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