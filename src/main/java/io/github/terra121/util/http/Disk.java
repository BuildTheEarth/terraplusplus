package io.github.terra121.util.http;

import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.common.function.io.IOPredicate;
import net.daporkchop.lib.common.function.io.IORunnable;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Handles disk I/O operations for {@link Http}.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class Disk {
    private final EventLoop DISK_EXECUTOR = new DefaultEventLoop(PThreadFactories.builder().daemon().minPriority().name("terra++ disk I/O thread").build());

    private final Path CACHE_ROOT;
    private final Path TMP_FILE;

    static {
        File mcRoot;
        try {
            mcRoot = FMLCommonHandler.instance().getSide().isClient()
                    ? Minecraft.getMinecraft().gameDir
                    : FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
        } catch (NullPointerException e) { //an NPE probably means we're running in a test environment, and FML isn't initialized
            if (!PFiles.checkDirectoryExists(mcRoot = new File("run"))) {
                mcRoot = new File(".");
            }
        }
        CACHE_ROOT = PFiles.ensureDirectoryExists(new File(mcRoot, "terraplusplus/cache")).toPath();
        TMP_FILE = CACHE_ROOT.resolve("tmp");

        DISK_EXECUTOR.scheduleWithFixedDelay((IORunnable) Disk::pruneCache, 1L, 60L, TimeUnit.MINUTES);
    }

    /**
     * Asynchronously reads a file's contents into a {@link ByteBuf}.
     *
     * @param file the file
     * @param ttl  whether or not to check if the file's TTL has expired. If older than this timestamp, the file will be
     *             treated as if it were missing.
     * @return a {@link CompletableFuture} which will be notified when the file has been read
     */
    public CompletableFuture<ByteBuf> read(@NonNull Path file, boolean ttl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.isRegularFile(file) || (ttl && hasExpired(file))) { //file doesn't exist or is expired
                    return null;
                }

                ByteBuf buf = null;
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    int size = toInt(channel.size(), "file size");
                    (buf = ByteBufAllocator.DEFAULT.ioBuffer(size, size)).writeBytes(channel, size);
                    checkState(!buf.isWritable(), "only read %d/%d bytes!", buf.readableBytes(), size);
                    return buf.retain();
                } finally {
                    ReferenceCountUtil.release(buf);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("unable to read file: " + file, e);
            }
        }, DISK_EXECUTOR);
    }

    /**
     * Asynchronously writes a file's contents, replacing the existing file if it already exists.
     *
     * @param file the file
     * @param data the data
     */
    public void write(@NonNull Path file, @NonNull ByteBuf data) {
        DISK_EXECUTOR.submit(() -> {
            try {
                try (FileChannel channel = FileChannel.open(TMP_FILE, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    data.readBytes(channel, data.readableBytes());
                    checkState(!data.isReadable(), "didn't write all bytes!");
                }

                Files.move(TMP_FILE, file, StandardCopyOption.REPLACE_EXISTING);
                Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                ReferenceCountUtil.release(data);
            }
        });
    }

    /**
     * Gets the file path which stores the cached data for the given url.
     *
     * @param url the url
     * @return the cache file
     */
    public Path cacheFileFor(@NonNull String url) {
        try {
            return CACHE_ROOT.resolve(Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    /**
     * Checks whether or not the given file's TTL has expired.
     *
     * @param file the file to check
     * @return whether or not the file's TTL has expired
     */
    public boolean hasExpired(@NonNull Path file) throws IOException {
        return System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(TerraConfig.data.cacheTTL) > Files.getLastModifiedTime(file).toMillis();
    }

    private void pruneCache() throws IOException {
        if (TerraMod.LOGGER != null && !TerraConfig.reducedConsoleMessages) {
            TerraMod.LOGGER.info("running cache cleanup...");
        }
        Files.list(CACHE_ROOT)
                .onClose(() -> {
                    if (TerraMod.LOGGER != null && !TerraConfig.reducedConsoleMessages) {
                        TerraMod.LOGGER.info("cache cleanup complete.");
                    }
                })
                .filter(Files::isRegularFile).filter((IOPredicate<Path>) Disk::hasExpired).forEach((IOConsumer<Path>) Files::delete);
    }
}
