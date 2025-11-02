package net.buildtheearth.terraminusminus.util.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.URL;

/**
 * Used as the failure cause for requests issued from {@link Http} when an unknown HTTP status code is encountered.
 *
 * @author DaPorkchop_
 */
@Getter
public class UnknownHttpStatusException extends IOException {
    protected final @NonNull HttpResponseStatus status;

    public UnknownHttpStatusException(@NonNull HttpResponseStatus status, @NonNull URL parsedUrl) {
        super("unexpected response from server for URL \"" + parsedUrl + "\": " + status);

        this.status = status;
    }
}
