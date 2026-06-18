package roomescape.common.config.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.common.exception.OutboundRateLimitException;
import roomescape.infrastructure.ratelimit.TokenBucketRateLimiter;

/**
 * 나가는 호출 Rate Limit 검증. 한도를 넘은 호출은 외부로 안 나가므로 getRequestCount 로 확인한다.
 */
class OutboundRateLimitInterceptorTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        respondWithDone();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 자체_한도를_넘기면_외부로_보내지_않고_즉시_거부한다() {
        var clock = new AtomicLong(0); // 고정 → 보충 없음
        var client = clientWith(new TokenBucketRateLimiter(2, 1.0, clock::get)); // 버스트 2

        assertThat(confirm(client)).contains("DONE");
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2); // 2개까지는 실제로 나갔다

        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2); // 3번째는 외부로 안 나갔다
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() {
        var clock = new AtomicLong(0);
        var client = clientWith(new TokenBucketRateLimiter(1, 1.0, clock::get)); // 초당 1

        assertThat(confirm(client)).contains("DONE");
        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L); // +1초 → +1 토큰

        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2); // 거부된 호출은 서버에 도달하지 않았다
    }

    private RestClient clientWith(TokenBucketRateLimiter rateLimiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .build();
    }

    private String confirm(RestClient client) {
        return client.post().uri("/v1/payments/confirm").retrieve().body(String.class);
    }

    /**
     * 큐 소진에 따른 멈춤 없이 항상 200 DONE 을 돌려주는 디스패처(거부된 호출은 어차피 도달하지 않는다).
     */
    private void respondWithDone() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"status\":\"DONE\"}");
            }
        });
    }

}
