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
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.exception.OutboundRateLimitException;

class OutboundRateLimitInterceptorTest {

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
    void 토큰이_있으면_통과하고_없으면_외부로_보내지_않고_거부한다() {
        // given : capacity 1, 보충 거의 없음
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 0.0001, () -> 0L);
        RestClient client = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(limiter))
                .build();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        // when : 첫 호출은 토큰을 소비하고 서버에 도달
        String body = client.get().uri("/v1/payments/confirm").retrieve().body(String.class);
        assertThat(body).isEqualTo("ok");

        // then : 두 번째 호출은 토큰이 없어 외부로 나가지 않고 거부된다
        assertThatThrownBy(() -> client.get().uri("/v1/payments/confirm").retrieve().body(String.class))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
