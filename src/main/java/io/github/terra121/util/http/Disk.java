package io.github.terra121.util.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Handles disk I/O operations for {@link Http}.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class Disk {
    private final EventLoop DISK_EXECUTOR = new DefaultEventLoop(PThreadFactories.builder().daemon().minPriority().name("terra++ disk I/O thread").build());

    /**
     * Asynchronously reads a file's contents into a {@link ByteBuf}.
     *
     * @param file the file
     * @return a {@link CompletableFuture} which will be notified when the file has been read
     */
    public CompletableFuture<ByteBuf> read(@NonNull File file) {
        return CompletableFuture.supplyAsync(() -> {
            if (PFiles.checkFileExists(file)) { //file doesn't exist, return null
                return null;
            }

            ByteBuf buf = null;
            try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
                int size = toInt(channel.size(), "file size");
                (buf = ByteBufAllocator.DEFAULT.ioBuffer(size, size)).writeBytes(channel, 0L, size);
                checkState(!buf.isWritable(), "only read %d/%d bytes!", buf.readableBytes(), size);
                return buf.retain();
            } catch (IOException e) {
                throw new UncheckedIOException("unable to read file: " + file, e);
            } finally {
                ReferenceCountUtil.release(buf);
            }
        }, DISK_EXECUTOR);
    }
}
