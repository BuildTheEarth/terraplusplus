package io.github.terra121.util.http;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URL;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
@EqualsAndHashCode
class Host {
    @NonNull
    protected final String host;
    protected final int port;

    protected final boolean ssl;

    public Host(@NonNull URL url) {
        this.host = url.getHost();
        this.port = Math.max(url.getPort(), url.getDefaultPort());

        switch (url.getProtocol()) {
            case "http":
                this.ssl = false;
                break;
            case "https":
                this.ssl = true;
                break;
            default:
                throw new IllegalArgumentException("unsupported protocol: " + url.getProtocol());
        }
    }
}
