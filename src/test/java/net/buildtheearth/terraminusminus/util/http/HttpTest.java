package net.buildtheearth.terraminusminus.util.http;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTest {
	
	private static final String CI_USER_AGENT = "Terra-- CI";
	
	private static final String[] TEST_URLS = {
			"https://duckduckgo.com/robots.txt"
	};
	
	private final byte[][] testContents = new byte[TEST_URLS.length][0];
	
	@BeforeEach
	void doReferenceRequests() throws IOException {
		for (int i = 0; i < TEST_URLS.length; i++) {
			URL url = new URL(TEST_URLS[i]);
			URLConnection con = url.openConnection();
			con.addRequestProperty("User-agent", CI_USER_AGENT);
			try (InputStream in = con.getInputStream()) {
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				int nRead;
				byte[] data = new byte[0];

				while ((nRead = in.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				  data = new byte[in.available()];
				}

				this.testContents[i] = buffer.toByteArray();
			}
		}
	}

	@Test
    @Timeout(value = 5, unit = SECONDS)
	void testGetRequests() {
		for (int i = 0; i < TEST_URLS.length; i++) {
			this.testUrl(TEST_URLS[i], testContents[i]);
		}
	}
	
	private void testUrl(String url, byte[] result) {
		CompletableFuture<ByteBuf> request = Http.get(url);
		ByteBuf buffer = request.join();
		
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		
		assertArrayEquals(result, bytes);
	}

}
