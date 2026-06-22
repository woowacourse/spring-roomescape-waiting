package roomescape.infrastructure.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import roomescape.exception.domain.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitClientInterceptorTest {

    private static final long SEC = 1_000_000_000L;

    MockWebServer server;

    @BeforeEach
    void startServer() {
        server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    private RestClient clientWith(OutboundRateLimitClientInterceptor interceptor) {
        return RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestInterceptor(interceptor)
                .build();
    }

    private void enqueue200() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"result\":\"ok\"}"));
    }

    @Test
    @DisplayName("토큰이 있으면 요청이 정상 전송된다")
    void 토큰_있으면_요청_통과() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1.0, () -> now[0]);
        OutboundRateLimitClientInterceptor interceptor = new OutboundRateLimitClientInterceptor(limiter);
        enqueue200();

        var response = clientWith(interceptor).get().uri("/test").retrieve().toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("토큰이 소진되면 OutboundRateLimitException을 던지고 외부 요청을 보내지 않는다")
    void 토큰_소진_시_외부_호출_없이_예외() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        OutboundRateLimitClientInterceptor interceptor = new OutboundRateLimitClientInterceptor(limiter);
        RestClient client = clientWith(interceptor);

        enqueue200();
        client.get().uri("/test").retrieve().toEntity(String.class);

        assertThatThrownBy(() ->
                client.get().uri("/test").retrieve().toEntity(String.class)
        ).isInstanceOf(OutboundRateLimitException.class);

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("capacity개 요청만 통과하고 이후 요청은 거부된다")
    void capacity개_통과_이후_거부() {
        int capacity = 3;
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, () -> now[0]);
        OutboundRateLimitClientInterceptor interceptor = new OutboundRateLimitClientInterceptor(limiter);
        RestClient client = clientWith(interceptor);

        for (int i = 0; i < capacity; i++) {
            enqueue200();
            client.get().uri("/test").retrieve().toEntity(String.class);
        }

        assertThatThrownBy(() ->
                client.get().uri("/test").retrieve().toEntity(String.class)
        ).isInstanceOf(OutboundRateLimitException.class);

        assertThat(server.getRequestCount()).isEqualTo(capacity);
    }

    @Test
    @DisplayName("토큰 소진 후 시간이 지나면 보충되어 다시 통과된다")
    void 토큰_보충_후_재통과() {
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> now[0]);
        OutboundRateLimitClientInterceptor interceptor = new OutboundRateLimitClientInterceptor(limiter);
        RestClient client = clientWith(interceptor);

        enqueue200();
        client.get().uri("/test").retrieve().toEntity(String.class);

        now[0] = SEC;
        enqueue200();
        var response = client.get().uri("/test").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("동시 요청에서 capacity개만 외부로 나가고 나머지는 차단된다")
    void 동시_요청_중_capacity개만_외부_전송() throws InterruptedException {
        int capacity = 5;
        int threads = 30;
        long[] now = {0L};
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1.0, () -> now[0]);
        OutboundRateLimitClientInterceptor interceptor = new OutboundRateLimitClientInterceptor(limiter);
        RestClient client = clientWith(interceptor);

        for (int i = 0; i < capacity; i++) {
            enqueue200();
        }

        ConcurrentResult result = runConcurrently(client, threads);

        assertThat(result.passed()).isEqualTo(capacity);
        assertThat(result.blocked()).isEqualTo(threads - capacity);
    }

    private record ConcurrentResult(int passed, int blocked) {}

    private ConcurrentResult runConcurrently(RestClient client, int threads) throws InterruptedException {
        AtomicInteger passed = new AtomicInteger(0);
        AtomicInteger blocked = new AtomicInteger(0);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            Thread.ofVirtual().start(
                    () -> executeTask(client, ready, done, passed, blocked));
        }

        done.await(5, TimeUnit.SECONDS);
        return new ConcurrentResult(passed.get(), blocked.get());
    }

    private void executeTask(RestClient client, CountDownLatch ready, CountDownLatch done,
                              AtomicInteger passed, AtomicInteger blocked) {
        ready.countDown();
        awaitQuietly(ready);
        try {
            client.get().uri("/test").retrieve().toEntity(String.class);
            passed.incrementAndGet();
        } catch (OutboundRateLimitException e) {
            blocked.incrementAndGet();
        } catch (Exception ignored) {
        }
        done.countDown();
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
