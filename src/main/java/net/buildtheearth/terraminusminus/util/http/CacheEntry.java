package net.buildtheearth.terraminusminus.util.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.buildtheearth.terraminusminus.TerraConfig;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class CacheEntry {
    public static final int CACHE_VERSION = 3;

    public static final int STATUS_NOT_FOUND = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_REDIRECT = 2;
    public static final int STATUS_NOT_MODIFIED = -1;

    @With
    public final int status;

    public final long time;
    public final long staleTime;
    public final long expireTime;
    public final String etag;

    public final String location;

    public final boolean noCache;

    public CacheEntry(@NonNull FullHttpResponse response, @NonNull URL parsed) throws IOException {
        HttpHeaders headers = response.headers();

        this.time = headers.getTimeMillis(HttpHeaderNames.DATE, System.currentTimeMillis());

        Map<String, String> map = headers.getAll(HttpHeaderNames.CACHE_CONTROL).stream()
                .map(s -> s.split(",")).flatMap(Arrays::stream).map(String::trim)
                .map(Pattern.compile("^(.*?)=(.*?)$")::matcher)
                .filter(Matcher::find)
                .collect(Collectors.toMap(m -> m.group(1), m -> m.group(2)));

        long maxAge = -1L;
        long maxStale = -1L;

        if (!map.isEmpty()) {
            this.noCache = map.containsKey("no-cache") || map.containsKey("no-store");

            try {
                maxAge = Long.parseLong(map.getOrDefault("max-age", "-1"));

                if (!map.containsKey("immutable")) {
                    maxStale = Long.parseLong(map.getOrDefault("stale-while-revalidate", "-1"));
                }
            } catch (Exception ignored) {
                maxAge = maxStale = -1L;
            }
        } else {
            this.noCache = false;
        }

        this.etag = headers.get(HttpHeaderNames.ETAG, null);

        this.staleTime = maxAge >= 0L ? this.time + TimeUnit.SECONDS.toMillis(maxAge)
                : this.etag != null ? this.time : -1L;

        long fallbackExpireTime = this.time + TimeUnit.MINUTES.toMillis(TerraConfig.http.cacheTTL);
        long expireTime = Math.max(
                headers.getTimeMillis(HttpHeaderNames.EXPIRES, fallbackExpireTime),
                maxStale >= 0L ? this.time + TimeUnit.SECONDS.toMillis(maxStale) : -1L);
        this.expireTime = expireTime < this.time ? fallbackExpireTime : expireTime;

        String location = null;
        switch (response.status().codeClass()) {
            case SUCCESS:
                this.status = STATUS_SUCCESS;
                break;
            case REDIRECTION:
                if (response.status().code() == 304) { //304 Not Modified
                    this.status = STATUS_NOT_MODIFIED;
                } else {
                    this.status = STATUS_REDIRECT;
                    location = headers.get(HttpHeaderNames.LOCATION);
                }
                break;
            case CLIENT_ERROR:
                if (response.status().code() == 404) { //404 Not Found
                    this.status = STATUS_NOT_FOUND;
                    break;
                }
            default: //failure
                throw new UnknownHttpStatusException(response.status(), parsed);
        }
        this.location = location;
    }

    public CacheEntry(@NonNull ByteBuf buf) {
        this.status = buf.readByte();

        this.time = buf.readLong();
        this.staleTime = buf.readLong();
        this.expireTime = buf.readLong();

        int len = buf.readInt();
        this.etag = len >= 0 ? buf.readCharSequence(len, StandardCharsets.US_ASCII).toString() : null;

        this.location = this.status == STATUS_REDIRECT ? buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString() : null;

        this.noCache = false;
    }

    public boolean isStale(long now) {
        return this.staleTime >= 0L && now >= this.staleTime;
    }

    public boolean isExpired(long now) {
        return now >= this.expireTime;
    }

    public void touch(@NonNull HttpHeaders headers) {
        if (this.etag != null) {
            headers.set(HttpHeaderNames.IF_NONE_MATCH, this.etag);
        } else if (this.staleTime >= 0L) {
            headers.set(HttpHeaderNames.IF_MODIFIED_SINCE, DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(this.time)));
        }
    }

    public void write(@NonNull ByteBuf buf) {
        buf.writeByte(this.status);

        buf.writeLong(this.time);
        buf.writeLong(this.staleTime);
        buf.writeLong(this.expireTime);

        if (this.etag != null) {
            buf.writeInt(this.etag.length()).writeCharSequence(this.etag, StandardCharsets.US_ASCII);
        } else {
            buf.writeInt(-1);
        }

        if (this.status == STATUS_REDIRECT) {
            int i = buf.writerIndex();
            int len = buf.writeInt(-1).writeCharSequence(this.location, StandardCharsets.UTF_8);
            buf.setInt(i, len);
        }
    }
}
