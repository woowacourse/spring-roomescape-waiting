package roomescape.payment.infrastructure.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.exception.OutboundRateLimitException;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossOutboundRateLimitInterceptorTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URI = BASE_URL + "/v1/payments/confirm";
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final AtomicLong currentNanos = new AtomicLong();

    @Test
    @DisplayName("나가는 호출 한도를 넘으면 외부로 보내지 않고 거부한다")
    void rejectOutboundRequestWithoutExternalCallWhenRateLimitExceeded() {
        final TestRestClient testRestClient = createTestRestClient(1, 1);
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("success", MediaType.TEXT_PLAIN));

        assertThat(postConfirm(testRestClient.restClient)).isEqualTo("success");
        assertThatThrownBy(() -> postConfirm(testRestClient.restClient))
                .isInstanceOf(OutboundRateLimitException.class)
                .hasMessageContaining("1초 후");

        testRestClient.server.verify();
    }

    @Test
    @DisplayName("나가는 호출 토큰이 보충되면 다시 외부로 보낸다")
    void sendOutboundRequestAfterTokenRefilled() {
        final TestRestClient testRestClient = createTestRestClient(1, 1);
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("first", MediaType.TEXT_PLAIN));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("second", MediaType.TEXT_PLAIN));

        assertThat(postConfirm(testRestClient.restClient)).isEqualTo("first");
        assertThatThrownBy(() -> postConfirm(testRestClient.restClient))
                .isInstanceOf(OutboundRateLimitException.class);

        currentNanos.addAndGet(NANOS_PER_SECOND);

        assertThat(postConfirm(testRestClient.restClient)).isEqualTo("second");
        testRestClient.server.verify();
    }

    private TestRestClient createTestRestClient(final int capacity, final int refillPerSec) {
        final TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(
                capacity,
                refillPerSec,
                currentNanos::get
        );
        final RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestInterceptor(new TossOutboundRateLimitInterceptor(rateLimiter));
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder)
                .build();

        return new TestRestClient(builder.build(), server);
    }

    private String postConfirm(final RestClient restClient) {
        return restClient.post()
                .uri("/v1/payments/confirm")
                .body("{}")
                .retrieve()
                .body(String.class);
    }

    private record TestRestClient(
            RestClient restClient,
            MockRestServiceServer server
    ) {
    }
}
