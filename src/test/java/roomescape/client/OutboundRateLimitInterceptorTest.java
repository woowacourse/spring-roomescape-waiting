package roomescape.client;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundRateLimitInterceptorTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        respondWithSuccess();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 자체_호출_한도를_넘으면_외부로_보내지_않고_거부한다() {
        AtomicLong clock = new AtomicLong(0);
        RestClient restClient = restClient(new TokenBucketRateLimiter(2, 1.0, clock::get));

        assertThat(confirm(restClient)).contains("DONE");
        assertThat(confirm(restClient)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        assertThatThrownBy(() -> confirm(restClient))
                .isInstanceOf(PaymentGatewayException.OutboundRateLimited.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 토큰이_보충되면_다시_외부로_보낸다() {
        AtomicLong clock = new AtomicLong(0);
        RestClient restClient = restClient(new TokenBucketRateLimiter(1, 1.0, clock::get));

        assertThat(confirm(restClient)).contains("DONE");
        assertThatThrownBy(() -> confirm(restClient))
                .isInstanceOf(PaymentGatewayException.OutboundRateLimited.class);

        clock.addAndGet(1_000_000_000L);

        assertThat(confirm(restClient)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private RestClient restClient(TokenBucketRateLimiter rateLimiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .build();
    }

    private String confirm(RestClient restClient) {
        return restClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);
    }

    private void respondWithSuccess() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                  "paymentKey": "payment-key",
                                  "orderId": "order-1",
                                  "status": "DONE",
                                  "totalAmount": 10000
                                }
                                """);
            }
        });
    }
}
