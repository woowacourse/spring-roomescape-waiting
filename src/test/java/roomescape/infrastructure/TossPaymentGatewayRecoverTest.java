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

/**
 * 실제 프록시 빈(@Retryable/@Recover 활성)을 띄워, MockWebServer로 지연/5xx 응답을 주고
 * 게이트웨이의 recover 동작을 검증한다.
 */
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
        // 이 테스트는 @DynamicPropertySource로 별도 컨텍스트라, 공유 인메모리 DB를 다른 @SpringBootTest 컨텍스트와
        // 동시에 쓰면 schema.sql의 CREATE TABLE이 충돌한다. 독립 인메모리 DB를 써서 격리한다.
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:toss-recover-test;DB_CLOSE_DELAY=-1");
    }

    /**
     * 타임아웃 값은 운영 코드에 상수로 박혀 있어(5s) 테스트에서 그대로 쓰면 느리다.
     * 무응답 시 빠르게 read timeout 나도록, 토스 요청 팩토리를 짧은 타임아웃 빈으로 교체한다.
     */
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
                    return new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE); // 무응답 → read timeout
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
