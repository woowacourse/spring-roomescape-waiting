package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
                    .tossRestClient(server.url("/").toString(), "secret-key", 100, 200);

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
}
