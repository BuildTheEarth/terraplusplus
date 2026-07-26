package net.buildtheearth.terraplusplus.util.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static org.apache.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTest {

    @Test
    void canMakeRequest() throws ExecutionException, InterruptedException, IOException {
        final String response = "Hello, World!";

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/hello", handler)) {
            endpoint.getAndAssertStringBody("?time=" + currentTimeMillis(), response);
        }
    }

    @Test
    void canCacheResponse() throws ExecutionException, InterruptedException, IOException {
        final String response = "Cached content";
        final String suffix = "?time=" + currentTimeMillis();

        final AtomicInteger requestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            requestCounter.incrementAndGet();
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Cache-Control", "max-age=100");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/withCache", handler)) {
            endpoint.getAndAssertStringBody(suffix, response);
            assertEquals(1, requestCounter.get());
            endpoint.getAndAssertStringBody(suffix, response); // Should hit the cache
            assertEquals(1, requestCounter.get());
        }
    }

    @Test
    void canRefreshStaleResponseFromEtag() throws ExecutionException, InterruptedException, IOException {
        final String response = "Cached content";
        final String etag = "foobar";
        final String suffix = "?time=" + currentTimeMillis();

        final AtomicInteger initialRequestCounter = new AtomicInteger(0);
        final AtomicInteger refreshRequestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                String expectedEtag = exchange.getRequestHeaders().getFirst("If-None-Match");
                if (expectedEtag != null && expectedEtag.equals(etag)) {
                    refreshRequestCounter.incrementAndGet();
                    exchange.sendResponseHeaders(SC_NOT_MODIFIED, -1);
                    return;
                }
                initialRequestCounter.incrementAndGet();
                    exchange.getResponseHeaders().set("Cache-Control", "max-age=1");
                    exchange.getResponseHeaders().set("Etag", etag);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
                    os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/withEtag", handler)) {
            endpoint.getAndAssertStringBody(suffix, response);
            assertEquals(1, initialRequestCounter.get());
            assertEquals(0, refreshRequestCounter.get());
            sleep(1_100);  // Let the cache expire
            endpoint.getAndAssertStringBody(suffix, response); // Should hit the cache but not be fresh
            assertEquals(1, initialRequestCounter.get());
            assertEquals(1, refreshRequestCounter.get());
        }
    }

    @Test
    void canRetryOnConnectionClosed() throws ExecutionException, InterruptedException, IOException {
        final String response = "Hello, World!";

        AtomicInteger counter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            boolean shouldFail = counter.incrementAndGet() < 3;
            try (OutputStream os = exchange.getResponseBody()) {
                if (shouldFail) {
                    return;
                }
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/retryUntilNotClosed", handler)) {
            endpoint.getAndAssertStringBody("?time=" + currentTimeMillis(), response);
        }
    }

    @Test
    void canRefreshStaleResponseFromLastModified() throws ExecutionException, InterruptedException, IOException {
        final String response = "Cached content";
        final String suffix = "?time=" + currentTimeMillis();

        final AtomicInteger initialRequestCounter = new AtomicInteger(0);
        final AtomicInteger refreshRequestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                boolean ifModifiedSince = exchange.getRequestHeaders().containsKey("If-Modified-Since");
                if (ifModifiedSince) {
                    refreshRequestCounter.incrementAndGet();
                    exchange.sendResponseHeaders(SC_NOT_MODIFIED, -1);
                    return;
                }
                initialRequestCounter.incrementAndGet();
                exchange.getResponseHeaders().set("Cache-Control", "max-age=1");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/withDate", handler)) {
            endpoint.getAndAssertStringBody(suffix, response);
            assertEquals(1, initialRequestCounter.get());
            assertEquals(0, refreshRequestCounter.get());
            sleep(1_100);  // Let the cache expire
            endpoint.getAndAssertStringBody(suffix, response); // Should hit the cache but not be fresh
            assertEquals(1, initialRequestCounter.get());
            assertEquals(1, refreshRequestCounter.get());
        }
    }

    static class TestHttpEndpoint implements Closeable {
        final InetSocketAddress address = new InetSocketAddress("127.0.0.1", randPort());
        final HttpServer server;
        final String uri;

        TestHttpEndpoint(String uri, HttpHandler handler) throws IOException {
            this.server = HttpServer.create(this.address, 0);
            this.uri = uri;
            server.createContext(uri, handler);
            this.server.start();
        }

        public String url() {
            return "http://" + this.address.getHostName() + ":" + this.address.getPort() + this.uri;
        }

        @Override
        public void close() {
            this.server.stop(10);
        }

        void getAndAssertStringBody(String suffix, String expectedBody) throws ExecutionException, InterruptedException {
            ByteBuf buffer = Http.get(this.url() + suffix).get();
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            String text = new String(data, StandardCharsets.UTF_8);
            assertEquals(expectedBody, text);
        }
    }

    private static int randPort() {
        Random random = new Random();
        return random.nextInt(65535 - 1024) + 1024;
    }

}
