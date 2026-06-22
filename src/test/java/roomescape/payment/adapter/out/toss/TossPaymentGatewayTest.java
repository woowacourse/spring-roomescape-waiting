package roomescape.payment.adapter.out.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestClient;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.ratelimit.RetryAfterInterceptor;

class TossPaymentGatewayTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String SECRET_KEY = "test_sk_example";
    private static final String IDEMPOTENCY_KEY = "idempotency-key";
    private static final String BASIC_TOKEN = Base64.getEncoder()
            .encodeToString((SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + BASIC_TOKEN);
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    @Test
    @DisplayName("결제 승인 요청은 Basic 인증 헤더와 paymentKey/orderId/amount JSON 바디를 전송한다.")
    void sends_confirm_request() {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic " + BASIC_TOKEN))
                .andExpect(header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order_123456",
                          "amount": 15000
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order_123456",
                          "status": "DONE",
                          "totalAmount": 15000
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(
                new PaymentConfirmation("payment-key", "order_123456", IDEMPOTENCY_KEY, 15_000)
        );

        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(15_000);
        server.verify();
    }

    @Test
    @DisplayName("토스 429 재시도에서도 같은 멱등키로 결제 승인 요청을 다시 보낸다.")
    void retries_with_same_idempotency_key_when_toss_returns_rate_limit() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + BASIC_TOKEN)
                .requestInterceptor(new RetryAfterInterceptor(2, Duration.ZERO, duration -> {
                }));
        MockRestServiceServer retryServer = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway retryGateway = new TossPaymentGateway(builder.build(), new ObjectMapper());

        retryServer.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, "0"));
        retryServer.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order_123456",
                          "status": "DONE",
                          "totalAmount": 15000
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = retryGateway.confirm(
                new PaymentConfirmation("payment-key", "order_123456", IDEMPOTENCY_KEY, 15_000)
        );

        assertThat(result.status()).isEqualTo("DONE");
        retryServer.verify();
    }

    @Test
    @DisplayName("이미 처리된 Toss 결제는 PAYMENT_ALREADY_PROCESSED 예외로 변환한다.")
    void maps_already_processed_payment() {
        expectError("ALREADY_PROCESSED_PAYMENT", withBadRequest());

        assertMappedError(ErrorCode.PAYMENT_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("카드 거절은 사용자 안내용 예외로 변환한다.")
    void maps_card_rejected() {
        expectError("REJECT_CARD_PAYMENT", withForbiddenRequest());

        assertMappedError(ErrorCode.PAYMENT_CARD_REJECTED);
    }

    @Test
    @DisplayName("토스 내부 오류는 재시도 대상 예외로 변환한다.")
    void maps_retryable_gateway_error() {
        expectError("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", withServerError());

        assertMappedError(ErrorCode.PAYMENT_GATEWAY_RETRYABLE);
    }

    @Test
    @DisplayName("멱등 요청 처리 중 에러는 다시 확인 가능한 예외로 변환한다.")
    void maps_idempotent_request_processing() {
        expectError("IDEMPOTENT_REQUEST_PROCESSING", withBadRequest());

        assertMappedError(ErrorCode.PAYMENT_IDEMPOTENT_REQUEST_PROCESSING);
    }

    @Test
    @DisplayName("미정의 Toss 에러 코드는 기본 결제 게이트웨이 예외로 변환한다.")
    void maps_unknown_gateway_error() {
        expectError("UNKNOWN_CODE", withBadRequest());

        assertMappedError(ErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    @Test
    @DisplayName("응답 지연으로 read timeout이 발생하면 확인 필요 예외로 변환한다.")
    void maps_read_timeout_to_unknown_result() throws Exception {
        HttpServer slowServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        slowServer.createContext("/v1/payments/confirm", exchange -> {
            try {
                Thread.sleep(1_000);
                byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        slowServer.start();

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(100));
            requestFactory.setReadTimeout(Duration.ofMillis(100));
            RestClient timeoutRestClient = RestClient.builder()
                    .baseUrl("http://localhost:" + slowServer.getAddress().getPort())
                    .requestFactory(requestFactory)
                    .build();
            TossPaymentGateway timeoutGateway = new TossPaymentGateway(timeoutRestClient, new ObjectMapper());

            assertThatThrownBy(() -> timeoutGateway.confirm(
                    new PaymentConfirmation("payment-key", "order_123456", IDEMPOTENCY_KEY, 15_000)
            ))
                    .isInstanceOf(EscapeRoomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_GATEWAY_TIMEOUT_UNKNOWN);
        } finally {
            slowServer.stop(0);
        }
    }

    private void expectError(String code, DefaultResponseCreator responseCreator) {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andRespond(responseCreator.body("""
                        {
                          "code": "%s",
                          "message": "error"
                        }
                        """.formatted(code)).contentType(MediaType.APPLICATION_JSON));
    }

    private void assertMappedError(ErrorCode errorCode) {
        assertThatThrownBy(() -> gateway.confirm(
                new PaymentConfirmation("payment-key", "order_123456", IDEMPOTENCY_KEY, 15_000)
        ))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
        server.verify();
    }
}
