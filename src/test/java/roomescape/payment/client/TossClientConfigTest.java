package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

class TossClientConfigTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void 응답이_느리면_read_timeout으로_끊는다() {
        server.enqueue(new MockResponse()
                .setBody("slow response")
                .setBodyDelay(1, TimeUnit.SECONDS));
        RestClient restClient = new TossClientConfig()
                .tossRestClient(server.url("/").toString(), "test_secret_key", 500, 100);

        assertThatThrownBy(() -> restClient.get()
                .uri("/")
                .retrieve()
                .body(String.class))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }
}
