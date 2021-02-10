package net.buildtheearth.terraplusplus.util.http;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URL;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode
class Host {
    @NonNull
    protected final String host;
    protected final int port;

    protected final boolean ssl;

    @EqualsAndHashCode.Exclude
    protected final String authority;

    public Host(@NonNull URL url) {
        this.host = url.getHost();
        this.port = Math.max(url.getPort(), url.getDefaultPort());
        this.authority = url.getAuthority();

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

    protected Host(@NonNull Host host) {
        this.host = host.host;
        this.port = host.port;
        this.ssl = host.ssl;
        this.authority = host.authority;
    }
}
