package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.payment.toss.TossPaymentGateway;
import roomescape.payment.toss.TossPaymentProperties;
import roomescape.ratelimit.RetryAfterInterceptor;

class TossPaymentGatewayTest {

    private static final String CONFIRM_URL = "https://example.com/v1/payments/confirm";

    @DisplayName("결제 승인 요청은 Basic 인증과 3필드 바디를 전송하고 도메인 결과로 변환한다.")
    @Test
    void confirmSuccess() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = newGateway(builder.build(), CONFIRM_URL);
        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString("test_sk_secret:".getBytes(StandardCharsets.UTF_8));

        server.expect(once(), requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(header("Idempotency-Key", "fixed-idempotency-key"))
                .andExpect(jsonPath("$.paymentKey").value("payment-key"))
                .andExpect(jsonPath("$.orderId").value("order-123456"))
                .andExpect(jsonPath("$.amount").value(23000))
                .andRespond(withSuccess(
                        """
                        {"paymentKey":"payment-key","orderId":"order-123456","totalAmount":23000,"status":"DONE"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        PaymentResult result = gateway.confirm(new PaymentConfirmation("payment-key", "order-123456", 23000, "fixed-idempotency-key"));

        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(23000);
        server.verify();
    }

    @DisplayName("Toss 주요 에러 코드를 도메인 예외로 변환한다.")
    @Test
    void confirmErrorMapping() {
        assertMapped("ALREADY_PROCESSED_PAYMENT", DomainErrorCode.PAYMENT_ALREADY_PROCESSED);
        assertMapped("REJECT_CARD_PAYMENT", DomainErrorCode.PAYMENT_REJECTED);
        assertMapped("UNAUTHORIZED_KEY", DomainErrorCode.PAYMENT_AUTHENTICATION_FAILED);
        assertMapped("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", DomainErrorCode.PAYMENT_RETRYABLE);
        assertMapped("UNKNOWN_CODE", DomainErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    private void assertMapped(String tossCode, DomainErrorCode expectedCode) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentGateway gateway = newGateway(builder.build(), CONFIRM_URL);

        server.expect(once(), requestTo(CONFIRM_URL))
                .andRespond(responseFor(tossCode));

        assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("payment-key", "order-123456", 23000, "fixed-idempotency-key")))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(expectedCode);
        server.verify();
    }

    private ResponseCreator responseFor(String tossCode) {
        String body = """
                {"code":"%s","message":"Toss error"}
                """.formatted(tossCode);
        if ("REJECT_CARD_PAYMENT".equals(tossCode)) {
            return withForbiddenRequest().body(body).contentType(MediaType.APPLICATION_JSON);
        }
        if ("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING".equals(tossCode)) {
            return withServerError().body(body).contentType(MediaType.APPLICATION_JSON);
        }
        return withBadRequest().body(body).contentType(MediaType.APPLICATION_JSON);
    }

    @DisplayName("Toss 응답 바디가 늦으면 설정된 read timeout으로 호출을 포기한다.")
    @Test
    void confirmReadTimeout() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBodyDelay(1, TimeUnit.SECONDS)
                    .setBody("""
                            {"paymentKey":"payment-key","orderId":"order-123456","totalAmount":23000,"status":"DONE"}
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
            server.start();

            TossPaymentGateway gateway = newGateway(
                    timeoutRestClient(Duration.ofMillis(500), Duration.ofMillis(150)),
                    server.url("/v1/payments/confirm").toString()
            );
            long startedAt = System.nanoTime();

            assertThatThrownBy(() -> gateway.confirm(new PaymentConfirmation("payment-key", "order-123456", 23000, "fixed-idempotency-key")))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("code")
                    .isEqualTo(DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN);

            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            assertThat(elapsedMillis)
                    .isGreaterThanOrEqualTo(100)
                    .isLessThan(900);
        }
    }

    @DisplayName("Toss가 429를 반환하면 Retry-After 후 같은 멱등키로 재시도한다.")
    @Test
    void retryAfterTooManyRequestsWithSameIdempotencyKey() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .addHeader(HttpHeaders.RETRY_AFTER, "0"));
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {"paymentKey":"payment-key","orderId":"order-123456","totalAmount":23000,"status":"DONE"}
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
            server.start();
            RestClient restClient = RestClient.builder()
                    .requestInterceptor(new RetryAfterInterceptor(2, Duration.ZERO, duration -> {
                    }))
                    .build();
            TossPaymentGateway gateway = newGateway(restClient, server.url("/v1/payments/confirm").toString());

            PaymentResult result = gateway.confirm(new PaymentConfirmation(
                    "payment-key",
                    "order-123456",
                    23000,
                    "fixed-idempotency-key"
            ));

            assertThat(result.status()).isEqualTo("DONE");
            assertThat(server.getRequestCount()).isEqualTo(2);
            assertThat(server.takeRequest().getHeader("Idempotency-Key")).isEqualTo("fixed-idempotency-key");
            assertThat(server.takeRequest().getHeader("Idempotency-Key")).isEqualTo("fixed-idempotency-key");
        }
    }

    private RestClient timeoutRestClient(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    private TossPaymentGateway newGateway(RestClient restClient, String confirmUrl) {
        return new TossPaymentGateway(
                restClient,
                new ObjectMapper(),
                new TossPaymentProperties("test_sk_secret", confirmUrl, Duration.ofSeconds(2), Duration.ofSeconds(5))
        );
    }
}
