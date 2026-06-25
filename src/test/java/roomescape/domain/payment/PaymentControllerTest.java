package roomescape.domain.payment;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 결제_승인_요청_성공_테스트() {
        given(paymentService.confirm(any(PaymentConfirmRequest.class)))
            .willReturn(new PaymentConfirmResponse("paymentKey", "orderId", 1000L, "DONE"));

        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", "paymentKey");
        params.put("orderId", "orderId");
        params.put("amount", 1000L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/payments/confirm")
            .then().log().all()
            .statusCode(200)
            .body("paymentKey", is("paymentKey"))
            .body("orderId", is("orderId"))
            .body("totalAmount", is(1000))
            .body("status", is("DONE"));
    }

    @Test
    void 결제_승인_요청값이_비어있으면_에러_반환_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", "");
        params.put("orderId", "orderId");
        params.put("amount", 1000L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/payments/confirm")
            .then().log().all()
            .statusCode(400)
            .body("message", is("paymentKey는 필수 입력 값입니다."));
    }

    @Test
    void 토스_승인_에러의_code와_message를_반환한다() {
        given(paymentService.confirm(any(PaymentConfirmRequest.class)))
            .willThrow(new PaymentException(
                HttpStatus.BAD_REQUEST,
                "ALREADY_PROCESSED_PAYMENT",
                "이미 처리된 결제 입니다."
            ));

        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", "paymentKey");
        params.put("orderId", "orderId");
        params.put("amount", 1000L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/payments/confirm")
            .then().log().all()
            .statusCode(400)
            .body("code", is("ALREADY_PROCESSED_PAYMENT"))
            .body("message", is("이미 처리된 결제 입니다."));
    }
}
