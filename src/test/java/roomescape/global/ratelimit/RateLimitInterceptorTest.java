package roomescape.global.ratelimit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.service.PaymentCommandService;
import roomescape.reservation.domain.Payment;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimitInterceptorTest {

    private static final String PAYMENT_KEY = "test_payment_key";

    @Autowired
    private TestDataHelper testHelper;

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @DisplayName("GET 요청은 토큰을 소모하지 않고 쓰기 요청만 한도 초과 시 429와 Retry-After를 반환합니다.")
    @Test
    void reject_unsafe_request_when_rate_limit_exceeded_without_consuming_get_request() {
        // Given: 토큰은 1개뿐이고 GET 요청을 여러 번 먼저 보냅니다.
        RestAssured.given()
                .when().get("/payments/config")
                .then().log().all()
                .statusCode(200)
                .body("clientKey", equalTo(tossClientKey))
                .body("$", not(org.hamcrest.Matchers.hasKey("secretKey")));

        RestAssured.given()
                .when().get("/payments/config")
                .then().log().all()
                .statusCode(200);

        // When: 첫 번째 쓰기 요청이 토큰 1개를 소비합니다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentFailRequestWithoutOrderId())
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);

        // Then: 두 번째 쓰기 요청은 컨트롤러에 도달하지 않고 429와 Retry-After를 받습니다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentFailRequestWithoutOrderId())
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }

    @DisplayName("한도 초과 결제 승인 요청은 게이트웨이를 호출하기 전에 거부합니다.")
    @Test
    void reject_payment_confirm_before_calling_gateway_when_rate_limit_exceeded() {
        // Given: 먼저 다른 쓰기 요청으로 토큰 1개를 모두 소비합니다.
        Payment payment = preparePayment();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentFailRequestWithoutOrderId())
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);

        // When & Then: 결제 승인 요청은 429로 끝나고 게이트웨이는 호출되지 않습니다.
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue());

        verify(paymentGateway, never()).confirm(any());
    }

    private Payment preparePayment() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        return paymentCommandService.prepare(reservationId);
    }

    private Map<String, Object> paymentConfirmRequest(Payment payment) {
        return Map.of(
                "paymentKey", PAYMENT_KEY,
                "orderId", payment.getOrderId().value(),
                "amount", payment.getAmount().value()
        );
    }

    private Map<String, String> paymentFailRequestWithoutOrderId() {
        return Map.of(
                "code", "PAY_PROCESS_CANCELED",
                "message", "사용자가 결제를 취소했습니다."
        );
    }
}
