package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

class RetryAfterInterceptorTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void 게이트웨이가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader(HttpHeaders.RETRY_AFTER, "1"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setBody("{\"status\":\"DONE\"}"));
        RestClient client = clientWith(3);

        String body = client.post().uri("/v1/payments/confirm").retrieve().body(String.class);

        assertThat(body).contains("DONE");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 재시도를_모두_소진해도_429면_게이트웨이_Rate_Limit_예외로_실패한다() {
        server.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "0"));
        server.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "0"));
        server.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "0"));
        RestClient client = clientWith(3);

        assertThatThrownBy(() -> client.post().uri("/v1/payments/confirm").retrieve().body(String.class))
                .isInstanceOf(GatewayRateLimitException.class)
                .hasFieldOrPropertyWithValue("code", GatewayRateLimitException.CODE);
        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    private RestClient clientWith(int maxAttempts) {
        return RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }
}
