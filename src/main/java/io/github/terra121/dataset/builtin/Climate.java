package io.github.terra121.dataset.builtin;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.projection.dymaxion.ConformalDynmaxionProjection;
import io.github.terra121.util.IntToDoubleBiFunction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import io.netty.util.AsciiString;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.util.PArrays;
import net.minecraftforge.fml.common.asm.transformers.deobf.LZMAInputSupplier;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

import static net.daporkchop.lib.common.util.PValidation.*;

public class Climate {
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

    @SneakyThrows(IOException.class)
    public static void main(String... args) {
        Matcher matcher;
        try (InputStream in = ConformalDynmaxionProjection.class.getResourceAsStream("conformal.txt")) {
            matcher = Pattern.compile("\\[(.*?), (.*?)]", Pattern.MULTILINE).matcher(new AsciiString(StreamUtil.toByteArray(in), false));
        }

        int SIDE_LENGTH = 256;
        ByteBuf buf = Unpooled.buffer();

        for (int v = 0; v < SIDE_LENGTH + 1; v++) {
            for (int u = 0; u < SIDE_LENGTH + 1 - v; u++) {
                checkState(matcher.find());
                buf.writeDouble(Double.parseDouble(matcher.group(1))).writeDouble(Double.parseDouble(matcher.group(2)));
            }
        }

        try (OutputStream out = new BZip2CompressorOutputStream(new FileOutputStream("/media/daporkchop/PortableIDE/Minecraft/terra121/src/main/resources/io/github/terra121/projection/dymaxion/conformal.bz2"))) {
            buf.readBytes(out, buf.readableBytes());
        }
    }

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