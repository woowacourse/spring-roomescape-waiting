package roomescape.payment.infrastructure.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.exception.PaymentCommunicationException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossRateLimitRetryInterceptorTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URI = BASE_URL + "/v1/payments/confirm";

    private final List<Duration> sleepDurations = new ArrayList<>();

    @Test
    @DisplayName("토스가 429와 Retry-After를 응답하면 해당 시간만큼 대기 후 재시도한다")
    void retryAfterRetryAfterHeader() {
        final TestRestClient testRestClient = createTestRestClient(3, Duration.ofSeconds(1));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "2"));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("success", MediaType.TEXT_PLAIN));

        final String response = postConfirm(testRestClient.restClient);

        assertThat(response).isEqualTo("success");
        assertThat(sleepDurations).containsExactly(Duration.ofSeconds(2));
        testRestClient.server.verify();
    }

    @Test
    @DisplayName("Retry-After가 없으면 기본 대기 시간으로 재시도한다")
    void retryAfterFallbackBackoffWhenRetryAfterHeaderIsMissing() {
        final TestRestClient testRestClient = createTestRestClient(3, Duration.ofSeconds(1));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("success", MediaType.TEXT_PLAIN));

        final String response = postConfirm(testRestClient.restClient);

        assertThat(response).isEqualTo("success");
        assertThat(sleepDurations).containsExactly(Duration.ofSeconds(1));
        testRestClient.server.verify();
    }

    @Test
    @DisplayName("최대 시도 횟수를 넘겨도 429면 결제 통신 예외로 실패한다")
    void throwPaymentCommunicationExceptionWhenMaxAttemptsExceeded() {
        final TestRestClient testRestClient = createTestRestClient(2, Duration.ofSeconds(1));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        testRestClient.server.expect(requestTo(CONFIRM_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> postConfirm(testRestClient.restClient))
                .isInstanceOf(PaymentCommunicationException.class)
                .hasMessageContaining("결제 승인 요청 한도");
        assertThat(sleepDurations).containsExactly(Duration.ofSeconds(1));
        testRestClient.server.verify();
    }

    private TestRestClient createTestRestClient(final int maxAttempts, final Duration fallbackBackoff) {
        final TossRateLimitRetryInterceptor interceptor = new TossRateLimitRetryInterceptor(
                maxAttempts,
                fallbackBackoff,
                sleepDurations::add
        );
        final RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestInterceptor(interceptor);
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
