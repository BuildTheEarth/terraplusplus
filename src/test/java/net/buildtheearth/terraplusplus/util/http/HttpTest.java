package net.buildtheearth.terraplusplus.util.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

@Execution(value = ExecutionMode.CONCURRENT)
public class HttpTest {

    @Test
    void canMakeRequest() throws ExecutionException, InterruptedException, IOException {
        final String response = "Hello, World!";

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
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
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
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
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/retryUntilNotClosed", handler)) {
            endpoint.getAndAssertStringBody("?time=" + currentTimeMillis(), response);
        }
    }

    @Test
    void canRetryOnConnectionTimeout() throws ExecutionException, InterruptedException, IOException {
        final String response = "Hello, World!";

        AtomicInteger counter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            boolean shouldTimeout = counter.incrementAndGet() < 2;
            if (shouldTimeout) {
                try {
                    sleep(5_000);  // Will trigger io.netty.handler.timeout.ReadTimeoutException in the client
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/retryUntilNotTimeout", handler)) {
            endpoint.getAndAssertStringBody("?time=" + currentTimeMillis(), response);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { SC_REQUEST_TIMEOUT, 429, SC_BAD_GATEWAY, SC_SERVICE_UNAVAILABLE, SC_GATEWAY_TIMEOUT})
    void canRetryOnHttpRetriableStatusCodes(int status) throws ExecutionException, InterruptedException, IOException {
        final String response = "Hello, World!";
        final String plzRetryResponse = "Please retry";

        AtomicInteger counter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
            boolean shouldBeUnavailable = counter.incrementAndGet() < 3;
                if (shouldBeUnavailable) {
                    exchange.sendResponseHeaders(status, plzRetryResponse.getBytes().length);
                    os.write(plzRetryResponse.getBytes());
                    return;
                }
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(SC_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/retrySuccess", handler)) {
            endpoint.getAndAssertStringBody("?time=" + currentTimeMillis(), response);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {SC_BAD_REQUEST, SC_UNAUTHORIZED, SC_FORBIDDEN, SC_NOT_FOUND, SC_METHOD_NOT_ALLOWED, SC_NOT_ACCEPTABLE, SC_GONE, SC_NOT_IMPLEMENTED})
    void doesNotRetryOnNonRetriableStatusCodes(final int status) {
        final String body = "plz don't retry";

        AtomicInteger counter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            counter.incrementAndGet();
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(status, body.getBytes().length);
                os.write(body.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/notRetryable", handler)) {
            Http.get(endpoint.url() + "?time=" + currentTimeMillis() + "&code=" + status).get();
        } catch (Throwable ignored) {
            // It's ok if it throws an exception, as long as it did not retry
        }

        assertEquals(1, counter.get());
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

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void respectsMaxTriesInRequestOptions(int maxTries) throws Exception {
        final String response = "Internal server errro";
        final AtomicInteger counter = new AtomicInteger();
        HttpHandler handler = exchange -> {
            counter.incrementAndGet();
            exchange.sendResponseHeaders(SC_INTERNAL_SERVER_ERROR, response.getBytes().length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint("/error", handler)) {
            Http.RequestOptions options = Http.RequestOptions.builder().maxTries(maxTries).build();
            try {
                Http.get(endpoint.url() + "?maxTries=" + maxTries, options).get();
            } catch (Exception ignored) {
                // It's ok to fail as long as it tried the expected number of times
            }
            assertEquals(maxTries, counter.get());
        }
    }

    @Test
    void isDoingExponentialBackoff() throws Exception {
        final String response = "Internal server error";
        final AtomicInteger counter = new AtomicInteger();
        final long[] timings = new long[5];
        HttpHandler handler = exchange -> {
            int i = counter.getAndIncrement();
            timings[i] = System.currentTimeMillis();
            exchange.sendResponseHeaders(SC_INTERNAL_SERVER_ERROR, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        };
        try (TestHttpEndpoint endpoint = new TestHttpEndpoint("/errorToCheckBackoff", handler)) {
            Http.RequestOptions options = Http.RequestOptions.builder()
                    .maxTries(timings.length)
                    .retryDelayJitterFactor(0f)
                    .build();
            try {
                Http.get(endpoint.url(), options).get();
            } catch (Exception ignored) {
                // It's ok to fail as long as it tried the expected number of times
            }
            assertEquals(timings.length, counter.get());
            long[] timingDeltas = new long[timings.length - 1];
            for (int i = 1; i < timings.length; i++) {
                timingDeltas[i-1] = timings[i] - timings[i - 1];
            }
            for (int i = 1; i < timingDeltas.length; i++) {
                assertTrue(timingDeltas[i] > timingDeltas[i - 1] * 2);
            }
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

        void getAndAssertStringBody(Http.RequestOptions options, String suffix, String expectedBody) throws ExecutionException, InterruptedException {
            ByteBuf buffer = Http.get(this.url() + suffix).get();
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            String text = new String(data, StandardCharsets.UTF_8);
            assertEquals(expectedBody, text);
        }
        void getAndAssertStringBody(String suffix, String expectedBody) throws ExecutionException, InterruptedException {
            this.getAndAssertStringBody(Http.DEFAULT_REQUEST_OPTIONS, suffix, expectedBody);
        }
    }

    private static int randPort() {
        Random random = new Random();
        return random.nextInt(65535 - 1024) + 1024;
    }

}
