package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentAuthException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;

class TossPaymentGatewayTest {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder, new ObjectMapper(),
                "https://api.tosspayments.com", "test_sk");
    }

    static Stream<Arguments> errorCases() {
        return Stream.of(
                Arguments.of(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", CardRejectedException.class),
                Arguments.of(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", PaymentAuthException.class),
                Arguments.of(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", PaymentAuthException.class),
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
        server.expect(requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"" + code + "\",\"message\":\"테스트 메시지\"}"));

        assertThatThrownBy(() -> gateway.confirm("pk_test", "order-1", 10000))
                .isInstanceOf(expected);

        server.verify();
    }
}
