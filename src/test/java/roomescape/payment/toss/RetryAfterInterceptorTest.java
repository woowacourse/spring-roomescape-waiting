package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 토스 429 백오프 재시도 인터셉터를 가짜 HTTP 서버(MockWebServer)로 검증한다.
 * 대기는 BackoffSleeper를 가짜로 주입해 실제로 자지 않고, 대기한 ms만 기록해 확인한다.
 */
class RetryAfterInterceptorTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void Retry_After를_받으면_그만큼_대기_후_재시도해_최종_200을_받는다() {
        List<Long> sleeps = new ArrayList<>();
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "2"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        RestClient client = clientWith(new RetryAfterInterceptor(3, 1000, sleeps::add));

        String body = client.get().uri("/x").retrieve().body(String.class);

        assertThat(body).isEqualTo("ok");
        assertThat(server.getRequestCount()).isEqualTo(2);
        assertThat(sleeps).containsExactly(2000L); // Retry-After 2초를 존중
    }

    @Test
    void Retry_After가_없으면_기본_백오프로_폴백해_재시도한다() {
        List<Long> sleeps = new ArrayList<>();
        server.enqueue(new MockResponse().setResponseCode(429));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        RestClient client = clientWith(new RetryAfterInterceptor(3, 1000, sleeps::add));

        client.get().uri("/x").retrieve().body(String.class);

        assertThat(sleeps).containsExactly(1000L);
    }

    @Test
    void maxAttempts를_넘어도_429면_마지막_429를_그대로_돌려준다() {
        List<Long> sleeps = new ArrayList<>();
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        RestClient client = clientWith(new RetryAfterInterceptor(2, 1000, sleeps::add));

        HttpStatusCode status = client.get().uri("/x")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> { }) // 기본 에러 처리(예외)를 흡수해 상태만 확인
                .toBodilessEntity()
                .getStatusCode();

        assertThat(status.value()).isEqualTo(429);
        assertThat(server.getRequestCount()).isEqualTo(2); // 최초 1 + 재시도 1 = maxAttempts
        assertThat(sleeps).containsExactly(1000L); // 재시도 1회만 대기
    }

    private RestClient clientWith(RetryAfterInterceptor interceptor) {
        // Apache 자동 재시도를 끈(운영과 동일) 팩토리 + 짧은 타임아웃 — 재시도는 오직 인터셉터가, 실패는 빠르게.
        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder.httpComponents()
                .withHttpClientCustomizer(HttpClientBuilder::disableAutomaticRetries)
                .build(ClientHttpRequestFactorySettings.defaults()
                        .withConnectTimeout(Duration.ofMillis(500))
                        .withReadTimeout(Duration.ofMillis(500)));
        return RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestFactory(factory)
                .requestInterceptor(interceptor)
                .build();
    }
}
