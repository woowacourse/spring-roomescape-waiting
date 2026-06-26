package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentRetryExhaustedException;

class RetryAfterInterceptorTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("429와 Retry-After를 주면 대기 후 재시도해 최종 200을 받는다")
    void retriesAfterWaitAndSucceeds() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        RestClient client = clientWith(3);

        String body = confirm(client);

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Retry-After가 없으면 짧은 고정 간격으로 폴백해 재시도한다")
    void fallsBackToFixedDelayWhenNoRetryAfterHeader() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        RestClient client = clientWith(3);

        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("maxAttempts를 넘어도 429면 도메인 예외로 실패한다")
    void failsWithDomainExceptionWhenRetriesExhausted() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        RestClient client = clientWith(2);

        assertThatThrownBy(() -> confirm(client))
                .isInstanceOf(PaymentRetryExhaustedException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private RestClient clientWith(int maxAttempts) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }

    private String confirm(RestClient client) {
        return client.post().uri("/v1/payments/confirm").retrieve().body(String.class);
    }
}
