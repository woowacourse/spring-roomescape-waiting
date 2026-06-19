package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class OutboundRateLimitInterceptorTest {

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

    private RestClient clientWith(long capacity, double refillPerSec) {
        return RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new OutboundRateLimitInterceptor(capacity, refillPerSec))
                .build();
    }

    @Test
    void 한도를_넘은_호출은_외부로_나가지_않고_거부된다() {
        RestClient client = clientWith(1, 0.001);
        server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        String first = client.get().uri(server.url("/").uri()).retrieve().body(String.class);
        assertThat(first).isEqualTo("ok");

        assertThatThrownBy(() -> client.get().uri(server.url("/").uri()).retrieve().body(String.class))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() throws InterruptedException {
        RestClient client = clientWith(1, 100);
        server.enqueue(new MockResponse().setResponseCode(200).setBody("first"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("second"));

        client.get().uri(server.url("/").uri()).retrieve().body(String.class);
        Thread.sleep(50);

        String second = client.get().uri(server.url("/").uri()).retrieve().body(String.class);

        assertThat(second).isEqualTo("second");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }
}
