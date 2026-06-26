package roomescape.payment.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RetryAfterInterceptorTest {

    private static final MockWebServer MOCK_WEB_SERVER = startServer();

    @AfterAll
    static void tearDown() throws IOException {
        MOCK_WEB_SERVER.shutdown();
    }

    @Test
    void 게이트웨이가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        MOCK_WEB_SERVER.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "1"));
        MOCK_WEB_SERVER.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        RestClient client = RestClient.builder()
                .baseUrl(MOCK_WEB_SERVER.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(3))
                .build();

        String body = client.post().uri("/v1/payments/confirm").retrieve().body(String.class);

        assertThat(body).contains("DONE");
        assertThat(MOCK_WEB_SERVER.getRequestCount()).isEqualTo(2);
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return server;
    }
}
