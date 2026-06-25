package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.common.config.OutboundRateLimitProperties;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.exception.PaymentErrorCode;

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

    private RestClient clientWith(int maxAttempts) {
        // fallbackSeconds=0, Retry-After=0 으로 대기 없이 빠르게 검증한다.
        RetryAfterInterceptor interceptor =
                new RetryAfterInterceptor(new OutboundRateLimitProperties(1, 1, maxAttempts, 0));
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(interceptor)
                .build();
    }

    @Test
    void _429를_받으면_Retry_After만큼_대기_후_재시도해_최종_200을_받는다() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        // when
        String body = clientWith(3).get().uri("/v1/payments/confirm").retrieve().body(String.class);

        // then
        assertThat(body).isEqualTo("ok");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void Retry_After가_없으면_고정_간격으로_폴백해_재시도한다() {
        // given : Retry-After 헤더 없음
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        // when
        String body = clientWith(3).get().uri("/v1/payments/confirm").retrieve().body(String.class);

        // then
        assertThat(body).isEqualTo("ok");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void maxAttempts를_넘어도_429면_도메인_예외로_실패한다() {
        // given : 계속 429
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        // when & then
        assertThatThrownBy(() -> clientWith(3).get().uri("/v1/payments/confirm").retrieve().body(String.class))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_RATE_LIMITED);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }
}
