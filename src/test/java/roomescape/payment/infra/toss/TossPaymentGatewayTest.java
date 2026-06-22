package roomescape.payment.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

class TossPaymentGatewayTest {

    @Test
    void UTF_8_Basic_인증과_승인_요청을_전송한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = gateway(builder);
        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString("test_sk_시크릿:".getBytes(StandardCharsets.UTF_8));

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", authorization))
                .andExpect(header("Idempotency-Key", "fixed-idempotency-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "ROOM_order123",
                          "amount": 10000
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "ROOM_order123",
                          "totalAmount": 10000,
                          "status": "DONE"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(
                new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L),
                "fixed-idempotency-key"
        );

        assertThat(result).isEqualTo(
                new PaymentResult("payment-key", "ROOM_order123", 10_000L, "DONE"));
        server.verify();
    }

    @ParameterizedTest
    @MethodSource("errorMappings")
    void Toss_에러코드를_도메인_예외로_변환한다(String tossCode, PaymentErrorCode expected) {
        PaymentException exception = TossPaymentErrorMapper.map(
                new TossErrorResponse(tossCode, "gateway message"));

        assertThat(exception.errorCode()).isEqualTo(expected);
        assertThat(exception.gatewayCode()).isEqualTo(tossCode);
    }

    @Test
    void 미정의_코드는_기본_게이트웨이_예외로_변환한다() {
        assertThatThrownBy(() -> {
            throw TossPaymentErrorMapper.map(new TossErrorResponse("NEW_UNKNOWN_CODE", "unknown"));
        })
                .isInstanceOf(PaymentException.class)
                .extracting(exception -> ((PaymentException) exception).errorCode())
                .isEqualTo(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
    }

    @Test
    void 응답이_느리면_read_timeout_안에_승인_불명확_예외로_실패한다() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setBody("""
                            {
                              "paymentKey": "payment-key",
                              "orderId": "ROOM_order123",
                              "totalAmount": 10000,
                              "status": "DONE"
                            }
                            """)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBodyDelay(1, TimeUnit.SECONDS));
            server.start();

            TossPaymentGateway gateway = gateway(
                    timeoutRestClient(server.url("/").toString(), Duration.ofMillis(150))
            );
            long startedAt = System.nanoTime();

            assertThatThrownBy(() -> gateway.confirm(
                    new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L),
                    "fixed-idempotency-key"
            ))
                    .isInstanceOf(PaymentException.class)
                    .extracting(exception -> ((PaymentException) exception).errorCode())
                    .isEqualTo(PaymentErrorCode.CONFIRMATION_UNKNOWN);

            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            assertThat(elapsed).isBetween(Duration.ofMillis(100), Duration.ofMillis(900));
        }
    }

    @Test
    void 연결이_거부되면_연결_실패_예외로_구분한다() throws IOException {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }
        TossPaymentGateway gateway = gateway(
                timeoutRestClient("http://127.0.0.1:" + unusedPort, Duration.ofMillis(150))
        );

        assertThatThrownBy(() -> gateway.confirm(
                new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L),
                "fixed-idempotency-key"
        ))
                .isInstanceOf(PaymentException.class)
                .extracting(exception -> ((PaymentException) exception).errorCode())
                .isEqualTo(PaymentErrorCode.GATEWAY_CONNECTION_FAILED);
    }

    private TossPaymentGateway gateway(RestClient.Builder builder) {
        return gateway(builder.build());
    }

    private TossPaymentGateway gateway(RestClient restClient) {
        return new TossPaymentGateway(
                restClient,
                new ObjectMapper(),
                new PaymentProperties(
                        new PaymentProperties.Toss(
                                "https://api.tosspayments.com",
                                "test_ck_test",
                                "test_sk_시크릿",
                                Duration.ofSeconds(2),
                                Duration.ofSeconds(3)
                        ),
                        10_000L
                )
        );
    }

    private RestClient timeoutRestClient(String baseUrl, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(150));
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    private static Stream<Arguments> errorMappings() {
        return Stream.of(
                Arguments.of("ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.ALREADY_PROCESSED),
                Arguments.of("DUPLICATED_ORDER_ID", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("INVALID_REQUEST", PaymentErrorCode.INVALID_REQUEST),
                Arguments.of("UNAUTHORIZED_KEY", PaymentErrorCode.INVALID_API_KEY),
                Arguments.of("INVALID_API_KEY", PaymentErrorCode.INVALID_API_KEY),
                Arguments.of("REJECT_CARD_PAYMENT", PaymentErrorCode.CARD_REJECTED),
                Arguments.of("NOT_FOUND_PAYMENT", PaymentErrorCode.PAYMENT_NOT_FOUND),
                Arguments.of(
                        "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        PaymentErrorCode.RETRYABLE_ERROR
                )
        );
    }
}
