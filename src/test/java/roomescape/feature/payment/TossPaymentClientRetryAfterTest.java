package roomescape.feature.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.feature.payment.config.TossPaymentProperties;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.dto.PaymentResponse;

/**
 * {@link RetryAfterInterceptor} 가 {@link TossPaymentClient} 에 부착돼 동작하는지를
 * MockRestServiceServer + 가짜 {@link Sleeper} 로 실시간 대기 없이 결정적으로 검증한다.
 */
class TossPaymentClientRetryAfterTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final PaymentApproveRequest REQUEST =
            new PaymentApproveRequest("test_order_id", PAYMENT_KEY, 1_000L);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration FALLBACK_DELAY = Duration.ofSeconds(1);
    private static final String SUCCESS_BODY = "{\"status\":\"DONE\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY);

    private MockRestServiceServer mockServer;
    private FakeSleeper fakeSleeper;
    private TossPaymentClient tossPaymentClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        fakeSleeper = new FakeSleeper();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(fakeSleeper, MAX_ATTEMPTS, FALLBACK_DELAY);
        TossPaymentProperties properties =
                new TossPaymentProperties(BASE_URL, "test_secret_key", Duration.ofSeconds(5), Duration.ofSeconds(10));

        tossPaymentClient = new TossPaymentClient(builder, properties, new ObjectMapper(), interceptor);
    }

    @Test
    void 토스가_429와_Retry_After를_주면_그만큼_대기_후_재시도해_최종_200을_받는다() {
        // given: 첫 응답은 429(Retry-After: 2초), 두 번째 응답은 200
        mockServer.expect(requestTo(CONFIRM_URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header(HttpHeaders.RETRY_AFTER, "2"));
        mockServer.expect(requestTo(CONFIRM_URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

        // when
        PaymentResponse response = tossPaymentClient.confirm(REQUEST);

        // then: Retry-After 만큼 한 번 대기한 뒤 최종 200을 받는다
        assertThat(response.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(fakeSleeper.sleptDurations()).containsExactly(Duration.ofSeconds(2));
        mockServer.verify();
    }

    @Test
    void Retry_After가_없으면_고정_1초_간격으로_폴백해_재시도한다() {
        // given: 429에 Retry-After 헤더가 없음 → 두 번째는 200
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        mockServer.expect(requestTo(CONFIRM_URL))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

        // when
        PaymentResponse response = tossPaymentClient.confirm(REQUEST);

        // then: 고정 폴백(1초)으로 대기 후 성공
        assertThat(response.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(fakeSleeper.sleptDurations()).containsExactly(FALLBACK_DELAY);
        mockServer.verify();
    }

    @Test
    void 재시도가_maxAttempts를_넘으면_도메인_예외로_실패한다() {
        // given: 매 시도마다 429
        mockServer.expect(times(MAX_ATTEMPTS), requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header(HttpHeaders.RETRY_AFTER, "1"));

        // when
        Throwable thrown = catchThrowable(() -> tossPaymentClient.confirm(REQUEST));

        // then: maxAttempts(3)회 시도(=대기 2회) 후 도메인 예외로 실패
        assertThat(thrown).isInstanceOf(PaymentRateLimitedException.class);
        assertThat(fakeSleeper.sleptDurations()).hasSize(MAX_ATTEMPTS - 1);
        mockServer.verify();
    }
}
