package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentResultUnknownException;

@SpringBootTest
class TossPaymentGatewayRecoverTest {

    private static final MockWebServer server = new MockWebServer();

    static {
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private PaymentGateway paymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> {
            String url = server.url("/").toString();
            return url.substring(0, url.length() - 1);
        });
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:toss-recover-test;DB_CLOSE_DELAY=-1");
    }

    @TestConfiguration
    static class ShortTimeoutConfig {

        @Bean
        @Primary
        ClientHttpRequestFactory shortTimeoutTossClientHttpRequestFactory() {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(300));
            requestFactory.setReadTimeout(Duration.ofMillis(300));
            return requestFactory;
        }
    }

    @AfterAll
    static void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    void confirm이_read_timeout이어도_상태조회로_DONE이면_성공으로_복구한다() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().startsWith("/v1/payments/confirm")) {
                    return new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE);
                }
                return jsonResponse(200,
                        "{\"paymentKey\":\"pk_test\",\"orderId\":\"order-1\",\"totalAmount\":10000,"
                                + "\"status\":\"DONE\",\"approvedAt\":\"2026-06-18T10:00:00+09:00\"}");
            }
        });

        PaymentResult result = paymentGateway.confirm("pk_test", "order-1", 10000);

        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(result.paymentKey()).isEqualTo("pk_test");
    }

    @Test
    void confirm과_상태조회_모두_응답이_없으면_확인필요_예외를_던진다() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE);
            }
        });

        assertThatThrownBy(() -> paymentGateway.confirm("pk_test", "order-1", 10000))
                .isInstanceOf(PaymentResultUnknownException.class);
    }

    @Test
    void 토스_5xx가_반복되면_재시도_소진_후_게이트웨이_예외를_던진다() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return jsonResponse(500,
                        "{\"code\":\"FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING\",\"message\":\"internal\"}");
            }
        });

        assertThatThrownBy(() -> paymentGateway.confirm("pk_test", "order-1", 10000))
                .isInstanceOf(PaymentInternalException.class);
    }

    private static MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
