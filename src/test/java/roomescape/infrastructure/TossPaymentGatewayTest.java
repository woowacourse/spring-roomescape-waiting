package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;

class TossPaymentGatewayTest {

    private MockWebServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(1));
        requestFactory.setReadTimeout(Duration.ofSeconds(1));

        gateway = new TossPaymentGateway(RestClient.builder(), requestFactory, new ObjectMapper(), baseUrl(), "test_sk");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private String baseUrl() {
        String url = server.url("/").toString();
        return url.substring(0, url.length() - 1); // 끝 슬래시 제거
    }

    // 참고: 401(UNAUTHORIZED) 코드 → PaymentAuthException 매핑은 TossExceptionHandlerTest(순수 단위)에서 검증한다.
    // simple 팩토리(HttpURLConnection)는 401 응답을 특수 처리해 onStatus 에러 분기를 타지 않는 JDK 한계가 있어,
    // 여기서는 HTTP 레벨로 검증하지 않는다.
    static Stream<Arguments> errorCases() {
        return Stream.of(
                Arguments.of(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", CardRejectedException.class),
                Arguments.of(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", InvalidPaymentRequestException.class),
                Arguments.of(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", InvalidPaymentRequestException.class),
                Arguments.of(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", InvalidPaymentRequestException.class),
                Arguments.of(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", PaymentNotFoundException.class),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        PaymentInternalException.class),
                Arguments.of(HttpStatus.BAD_REQUEST, "SOME_UNDEFINED_CODE", PaymentConfirmException.class)
        );
    }

    @ParameterizedTest(name = "[{1}] -> {2}")
    @MethodSource("errorCases")
    void 토스_에러코드를_도메인_예외로_변환한다(HttpStatus status, String code, Class<? extends RuntimeException> expected) {
        // 모든 요청에 동일한 에러 응답을 준다. (401 등에서 HttpURLConnection이 재요청해도 같은 에러를 받도록)
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(status.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"code\":\"" + code + "\",\"message\":\"테스트 메시지\"}");
            }
        });

        assertThatThrownBy(() -> gateway.confirm("pk_test", "order-1", 10000))
                .isInstanceOf(expected);
    }

    @Test
    void 승인_요청에_멱등키_헤더로_orderId를_담아_보낸다() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"테스트 메시지\"}"));

        assertThatThrownBy(() -> gateway.confirm("pk_test", "order-1", 10000))
                .isInstanceOf(PaymentNotFoundException.class);

        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getHeader("Idempotency-Key")).isEqualTo("order-1");
    }
}
