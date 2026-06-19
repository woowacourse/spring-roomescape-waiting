package roomescape.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class RestClientTimeoutLearningTest {

    @DisplayName("연결 거부는 ResourceAccessException으로 표면화되고 root cause에 ConnectException이 남는다.")
    @Test
    void connectRefused() {
        RestClient restClient = restClient(Duration.ofMillis(100), Duration.ofMillis(500));

        assertThatThrownBy(() -> restClient.get()
                .uri("http://127.0.0.1:1")
                .retrieve()
                .body(String.class)
        )
                .isInstanceOf(ResourceAccessException.class)
                .satisfies(exception -> assertThat(rootCause(exception)).isInstanceOf(ConnectException.class));
    }

    @Disabled("블랙홀 IP는 네트워크 환경에 따라 즉시 실패할 수 있어 수동 학습용으로만 실행한다.")
    @DisplayName("SYN에 응답하지 않는 블랙홀 IP는 connect timeout 근처에서 포기한다.")
    @Test
    void connectTimeoutToBlackholeIp() {
        RestClient restClient = restClient(Duration.ofMillis(300), Duration.ofSeconds(2));
        long startedAt = System.nanoTime();

        assertThatThrownBy(() -> restClient.get()
                .uri("http://10.255.255.1:81")
                .retrieve()
                .body(String.class)
        )
                .isInstanceOf(ResourceAccessException.class)
                .satisfies(exception -> assertThat(rootCause(exception))
                        .isInstanceOfAny(SocketTimeoutException.class, ConnectException.class));

        long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        assertThat(elapsedMillis)
                .isGreaterThanOrEqualTo(250)
                .isLessThan(1500);
    }

    @DisplayName("응답 바디가 늦으면 read timeout 근처에서 포기하고 root cause에 SocketTimeoutException이 남는다.")
    @Test
    void readTimeout() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBodyDelay(1, java.util.concurrent.TimeUnit.SECONDS)
                    .setBody("slow response"));
            server.start();

            RestClient restClient = restClient(Duration.ofMillis(500), Duration.ofMillis(150));
            long startedAt = System.nanoTime();

            assertThatThrownBy(() -> restClient.get()
                    .uri(server.url("/slow").toString())
                    .retrieve()
                    .body(String.class)
            )
                    .isInstanceOf(RestClientException.class)
                    .satisfies(exception -> assertThat(rootCause(exception)).isInstanceOf(SocketTimeoutException.class));

            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            assertThat(elapsedMillis)
                    .isGreaterThanOrEqualTo(100)
                    .isLessThan(900);
        }
    }

    private RestClient restClient(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
