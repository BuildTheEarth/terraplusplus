package net.buildtheearth.terraminusminus.util.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.TerraConfig;
import net.buildtheearth.terraminusminus.TerraMinusMinus;
import net.buildtheearth.terraminusminus.util.Hex;
import net.daporkchop.lib.binary.netty.PUnpooled;
import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.common.function.io.IOPredicate;
import net.daporkchop.lib.common.function.io.IORunnable;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;

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
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

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
        File mcRoot = new File(".");
        CACHE_ROOT = PFiles.ensureDirectoryExists(new File(mcRoot, "terraplusplus/cache")).toPath();

        TMP_FILE = CACHE_ROOT.resolve("tmp");
        PFiles.rm(TMP_FILE.toFile()); //delete temp file if it exists

        //periodically prune the cache
        DISK_EXECUTOR.scheduleWithFixedDelay((IORunnable) Disk::pruneCache, 1L, 60L, TimeUnit.MINUTES);
    }

    /**
     * Asynchronously reads a file's contents into a {@link ByteBuf}.
     *
     * @param file the file
     * @return a {@link CompletableFuture} which will be notified when the file has been read
     */
    public CompletableFuture<ByteBuf> read(@NonNull Path file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(file)) { //file doesn't exist
                    return null;
                }

                ByteBuf buf = null;
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    int size = toInt(channel.size(), "file size");
                    buf = ByteBufAllocator.DEFAULT.ioBuffer(size, size);
                    for (int i = 0; i < size; i += buf.writeBytes(channel, i, size - i)) {
                    }
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
                    while (data.isReadable()) {
                        data.readBytes(channel, data.readableBytes());
                    }
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
     * Gets the path to an additional configuration file with the given name.
     *
     * @param name the configuration file's name
     * @return the path to the configuration file
     */
    public Path configFile(@NonNull String name) {
        return CACHE_ROOT.resolveSibling("config").resolve(name);
    }

    private void pruneCache() throws IOException {
        if (!TerraConfig.reducedConsoleMessages) {
            TerraMinusMinus.LOGGER.info("running cache cleanup...");
        }

        LongAdder count = new LongAdder();
        LongAdder size = new LongAdder();

        long now = System.currentTimeMillis();

        try (Stream<Path> stream = Files.list(CACHE_ROOT)) {
            stream.filter(Files::isRegularFile)
                    .filter((IOPredicate<Path>) p -> {
                        try (FileChannel channel = FileChannel.open(p, StandardOpenOption.READ)) {
                            long chSize = channel.size();
                            try {
                                ByteBuf buf = PUnpooled.wrap(channel.map(FileChannel.MapMode.READ_ONLY, 0L, chSize), toInt(chSize), true);
                                try {
                                    if (buf.readByte() == CacheEntry.CACHE_VERSION && !new CacheEntry(buf).isExpired(now)) { //file isn't expired, skip it
                                        return false;
                                    }
                                } finally {
                                    buf.release();
                                }
                            } catch (Throwable ignored) {
                                //no-op
                            }

                            //delete file
                            count.increment();
                            size.add(chSize);
                            return true;
                        }
                    })
                    .peek((IOConsumer<Path>) path -> {
                        count.increment();
                        size.add(Files.size(path));
                    })
                    .forEach((IOConsumer<Path>) Files::delete);
        } catch (Throwable e) {
            TerraMinusMinus.LOGGER.error("exception occurred during cache cleanup!", e);
        } finally {
            if (!TerraConfig.reducedConsoleMessages) {
                double mib = Math.round(size.sum() / (1024.0d * 1024.0d) * 10.0d) / 10.0d;
                TerraMinusMinus.LOGGER.info("cache cleanup complete. deleted {} old files, totalling {} bytes ({} MiB)", count.sum(), size.sum(), mib);
            }
        }
    }
}
