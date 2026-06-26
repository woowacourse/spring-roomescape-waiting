package roomescape.learning;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * learning-test-2-timeout
 *
 * <p>느린 서버를 흉내 내 connect/read timeout이 "얼마나 기다렸다 어떤 예외로" 실패하는지를
 * 경과 시간 단언으로 체감한다. 핵심은 두 가지다.
 * <ul>
 *   <li>타임아웃이 있으면 느린 호출을 <b>일찍 포기</b>한다(스레드를 무한정 잡지 않는다).</li>
 *   <li>연결 단계 실패와 읽기 단계 실패는 <b>표면화되는 예외/근본 원인</b>이 다르다.</li>
 * </ul>
 */
class TimeoutLearningTest {

    private static final int CONNECT_TIMEOUT_MS = 1_000;
    private static final int READ_TIMEOUT_MS = 500;

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

    private RestClient clientFor(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Test
    @DisplayName("응답 바디가 read timeout보다 늦게 오면, read timeout 만큼만 기다린 뒤 SocketTimeout(Read timed out)으로 실패한다")
    void readTimeout() {
        // 헤더는 즉시, 바디는 3초 뒤 → 0.5초 read timeout에 걸린다
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}")
                .setBodyDelay(3, TimeUnit.SECONDS));
        RestClient client = clientFor(mockWebServer.url("/").toString());

        long start = System.nanoTime();
        Throwable thrown = catchThrowable(() -> client.post().uri("/confirm").retrieve().body(String.class));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        // 일찍 포기: read timeout(0.5s) 이상 기다렸지만 서버 지연(3s)까지 붙잡히지 않았다
        assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(READ_TIMEOUT_MS - 100));
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));

        // 읽기 단계 실패 → RestClientException, 근본 원인은 SocketTimeoutException("Read timed out")
        assertThat(thrown).isInstanceOf(RestClientException.class);
        Throwable root = NestedExceptionUtils.getMostSpecificCause(thrown);
        assertThat(root).isInstanceOf(SocketTimeoutException.class);
        assertThat(root.getMessage()).containsIgnoringCase("read timed out");
    }

    @Test
    @DisplayName("connect timeout이 없으면 OS 기본값까지 스레드를 잡는다 — 블랙홀 IP로 connect timeout 만큼만 기다린 뒤 SocketTimeout(Connect timed out)으로 실패한다")
    void connectTimeout() {
        // SYN에 무응답인 블랙홀 IP. 닫힌 포트(즉시 거부)가 아니라 '연결 자체가 성립되지 않는' 진짜 connect timeout 재현
        RestClient client = clientFor("http://10.255.255.1:81");

        long start = System.nanoTime();
        Throwable thrown = catchThrowable(() -> client.get().uri("/").retrieve().body(String.class));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        // connect timeout(1s)에 의해 일찍 포기 — OS 기본값(수십 초)까지 가지 않는다
        assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(CONNECT_TIMEOUT_MS - 200));
        assertThat(elapsed).isLessThan(Duration.ofSeconds(10));

        // 연결 단계 실패 → ResourceAccessException, 근본 원인은 SocketTimeoutException("Connect timed out")
        assertThat(thrown).isInstanceOf(ResourceAccessException.class);
        Throwable root = NestedExceptionUtils.getMostSpecificCause(thrown);
        assertThat(root).isInstanceOf(SocketTimeoutException.class);
        assertThat(root.getMessage()).containsIgnoringCase("timed out");
    }

    @Test
    @DisplayName("느린 호출을 일찍 포기해야 성공 TPS가 유지된다 — 느린 1건이 빠른 N건을 막지 않는다")
    void slowCallShouldNotBlockFastCalls() {
        // 빠른 응답 3건 + 느린 응답 1건을 큐에 넣는다. 느린 건은 read timeout으로 잘려나가고 나머지는 즉시 성공한다.
        for (int i = 0; i < 3; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        }
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("slow").setBodyDelay(3, TimeUnit.SECONDS));
        RestClient client = clientFor(mockWebServer.url("/").toString());

        int success = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 4; i++) {
            try {
                client.get().uri("/").retrieve().body(String.class);
                success++;
            } catch (RestClientException ignored) {
                // 느린 호출은 read timeout으로 포기
            }
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(success).isEqualTo(3);
        // 느린 1건이 read timeout(0.5s)으로 잘려 전체가 서버 지연(3s)에 묶이지 않는다
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));
    }
}
