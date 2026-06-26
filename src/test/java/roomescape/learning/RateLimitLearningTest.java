package roomescape.learning;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.RestClient;
import roomescape.payment.OutboundRateLimitException;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * learning-test-3-ratelimit
 *
 * <p>토큰 버킷 하나(capacity·refillPerSec)로 순간 버스트와 평균 처리량 상한을 어떻게 거는지,
 * 그리고 같은 알고리즘을 방향만 바꿔 들어오는 요청(서버 입장)과 나가는 호출(클라이언트 입장)에
 * 어떻게 적용하는지를 결정적 가짜 시계로 확인한다.
 */
class RateLimitLearningTest {

    private final AtomicLong nanos = new AtomicLong(0);

    private TokenBucketRateLimiter limiter(long capacity, double refillPerSec) {
        return new TokenBucketRateLimiter(capacity, refillPerSec, nanos::get);
    }

    private void advanceSeconds(double seconds) {
        nanos.addAndGet((long) (seconds * 1_000_000_000L));
    }

    @Test
    @DisplayName("capacity는 순간 버스트 — 가득 찬 버킷은 한 번에 capacity개까지 통과시킨다")
    void capacityIsBurst() {
        TokenBucketRateLimiter limiter = limiter(5, 1);

        int passed = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.tryConsume()) {
                passed++;
            }
        }

        assertThat(passed).isEqualTo(5);
    }

    @Test
    @DisplayName("refillPerSec는 평균 처리량 상한 — 길게 보면 초당 refillPerSec개로 수렴한다")
    void refillIsAverageThroughput() {
        TokenBucketRateLimiter limiter = limiter(5, 2);
        drain(limiter);

        int passed = 0;
        for (int second = 0; second < 10; second++) {
            advanceSeconds(1);
            while (limiter.tryConsume()) {
                passed++;
            }
        }

        assertThat(passed).isEqualTo(20);
    }

    @Test
    @DisplayName("같은 토큰 버킷을 방향만 바꿔 들어오는 요청과 나가는 호출에 적용한다")
    void sameAlgorithmBothDirections() throws IOException {
        RateLimitInterceptor inbound = new RateLimitInterceptor(limiter(1, 1));
        inbound.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
        MockHttpServletResponse rejected = new MockHttpServletResponse();
        boolean allowed = inbound.preHandle(new MockHttpServletRequest(), rejected, new Object());

        assertThat(allowed).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(rejected.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1");

        MockWebServer server = new MockWebServer();
        server.start();
        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
            RestClient client = RestClient.builder()
                    .baseUrl(server.url("/").toString())
                    .requestInterceptor(new OutboundRateLimitInterceptor(limiter(1, 1)))
                    .build();

            assertThat(client.get().uri("/").retrieve().body(String.class)).isEqualTo("ok");
            assertThatThrownBy(() -> client.get().uri("/").retrieve().body(String.class))
                    .isInstanceOf(OutboundRateLimitException.class);
            assertThat(server.getRequestCount()).isEqualTo(1);
        } finally {
            server.shutdown();
        }
    }

    private void drain(TokenBucketRateLimiter limiter) {
        while (limiter.tryConsume()) {
        }
    }
}
