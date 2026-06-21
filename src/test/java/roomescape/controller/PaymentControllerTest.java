package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.DatabaseCleaner;
import roomescape.dto.response.PaymentConfirmResponse;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;
import roomescape.service.PaymentService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 결제_승인_완료_시_200을_반환한다() {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willReturn(PaymentConfirmResponse.confirmed());

        RestAssured.given().log().all()
                .queryParam("paymentKey", "test_pk")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 10000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void read_timeout으로_결제_결과_불명확_시_202를_반환한다() {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willReturn(PaymentConfirmResponse.uncertain());

        RestAssured.given().log().all()
                .queryParam("paymentKey", "test_pk")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 10000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(202);
    }

    @ParameterizedTest(name = "[{0}] → HTTP {1}")
    @MethodSource("paymentErrorCases")
    void 에러코드별_HTTP_상태코드와_응답_body가_올바르게_반환된다(
            PaymentErrorCode errorCode, int expectedHttpStatus) {

        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new PaymentException(errorCode));

        RestAssured.given().log().all()
                .queryParam("paymentKey", "test_pk")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 10000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(expectedHttpStatus)
                .body("exceptionCode", is(errorCode.getCode()))
                .body("message", is(errorCode.getMessage()));
    }

    static Stream<Arguments> paymentErrorCases() {
        return Stream.of(
                arguments(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH, 400),
                arguments(PaymentErrorCode.ALREADY_PROCESSED_PAYMENT, 400),
                arguments(PaymentErrorCode.REJECT_CARD_PAYMENT, 400),
                arguments(PaymentErrorCode.UNAUTHORIZED_KEY, 500),
                arguments(PaymentErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING, 500),
                arguments(PaymentErrorCode.PAYMENT_GATEWAY_ERROR, 500),
                arguments(PaymentErrorCode.PAYMENT_CONNECT_FAILED, 503)
        );
    }

    @Test
    void failUrl_orderId_없는_취소는_cancel을_호출하지_않고_200을_반환한다() {
        RestAssured.given().log().all()
                .queryParam("code", "PAY_PROCESS_CANCELED")
                .queryParam("message", "사용자가 결제를 취소했습니다.")
                .when().get("/payments/fail")
                .then().log().all()
                .statusCode(200);

        verify(paymentService, never()).cancel(anyString());
    }

    @Test
    void failUrl_orderId_있는_실패는_cancel을_호출하고_200을_반환한다() {
        String orderId = "order-abc123";

        RestAssured.given().log().all()
                .queryParam("code", "NOT_FOUND_PAYMENT_SESSION")
                .queryParam("message", "결제 시간이 만료되었습니다.")
                .queryParam("orderId", orderId)
                .when().get("/payments/fail")
                .then().log().all()
                .statusCode(200);

        verify(paymentService).cancel(orderId);
    }
}
