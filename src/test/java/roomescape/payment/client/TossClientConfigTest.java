package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class TossClientConfigTest {

    @Test
    @DisplayName("Toss RestClient는 응답 본문이 느리면 read timeout 시간 근처에서 포기한다.")
    void tossRestClientStopsWaitingAfterReadTimeout() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBodyDelay(1_500, TimeUnit.MILLISECONDS)
                    .setBody("""
                            {
                              "paymentKey": "payment-key",
                              "orderId": "order_id",
                              "status": "DONE",
                              "totalAmount": 5000
                            }
                            """));
            server.start();

            RestClient restClient = new TossClientConfig()
                    .tossRestClient(server.url("/").toString(), "secret-key", 100, 200, 1, 1, 100, 100);

            long startedAt = System.nanoTime();
            Throwable thrown = catchThrowable(() -> restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class));
            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

            assertThat(thrown)
                    .isInstanceOf(RestClientException.class)
                    .hasRootCauseInstanceOf(SocketTimeoutException.class);
            assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(150));
            assertThat(elapsed).isLessThan(Duration.ofMillis(1_200));
        }
    }

    @Test
    @DisplayName("Toss RestClient는 429 응답을 받으면 Retry-After 이후 다시 요청한다.")
    void tossRestClientRetriesAfterTooManyRequests() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0"));
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("ok"));
            server.start();

            RestClient restClient = new TossClientConfig()
                    .tossRestClient(server.url("/").toString(), "secret-key", 1_000, 1_000, 2, 1, 100, 100);

            String body = restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class);

            assertThat(body).isEqualTo("ok");
            assertThat(server.getRequestCount()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Toss RestClient는 maxAttempts까지 429가 계속되면 RateLimited 예외로 실패한다.")
    void tossRestClientThrowsWhenTooManyRequestsContinues() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0"));
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0"));
            server.start();

            RestClient restClient = new TossClientConfig()
                    .tossRestClient(server.url("/").toString(), "secret-key", 1_000, 1_000, 2, 1, 100, 100);

            assertThatThrownBy(() -> restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class))
                    .isInstanceOf(TossPaymentException.RateLimited.class);
            assertThat(server.getRequestCount()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Toss RestClient는 나가는 자체 한도를 넘으면 외부로 요청을 보내지 않는다.")
    void tossRestClientRejectsBeforeSendingWhenOutboundRateLimitIsExceeded() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("ok"));
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("ok"));
            server.start();

            RestClient restClient = new TossClientConfig()
                    .tossRestClient(server.url("/").toString(), "secret-key", 1_000, 1_000, 1, 1, 1, 0.1);

            restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class);

            assertThatThrownBy(() -> restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class))
                    .isInstanceOf(OutboundRateLimitException.class);
            assertThat(server.getRequestCount()).isEqualTo(1);
        }
    }
}
