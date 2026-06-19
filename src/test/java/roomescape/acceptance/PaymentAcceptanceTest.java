package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.fixture.Fixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentAcceptanceTest {

    private static final String AUTHORIZATION = "Authorization";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("POST /payments/success - 주문 결제를 승인하고 승인 결과를 반환한다")
    void confirmPayment() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        long reservationId = createReservation(slot);
        String orderId = orderIdOf(slot.bearer(), reservationId);
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", orderId, PaymentStatus.DONE, 50000L));

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .queryParam("paymentKey", "payment-key")
                .queryParam("orderId", orderId)
                .queryParam("amount", 50000)
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("status", equalTo("DONE"))
                .body("approvedAmount", equalTo(50000));
    }

    @Test
    @DisplayName("POST /payments/success - 요청 금액이 주문 금액과 다르면 400과 메시지를 반환한다")
    void confirmPaymentReturns400OnAmountMismatch() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        long reservationId = createReservation(slot);
        String orderId = orderIdOf(slot.bearer(), reservationId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .queryParam("paymentKey", "payment-key")
                .queryParam("orderId", orderId)
                .queryParam("amount", 9999)
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("PAYMENT_AMOUNT_MISMATCH"));
    }

    @Test
    @DisplayName("POST /payments/success - 토큰이 없으면 401과 메시지를 반환한다")
    void confirmPaymentReturns401WithoutToken() {
        RestAssured.given().log().all()
                .queryParam("paymentKey", "payment-key")
                .queryParam("orderId", "order-12345")
                .queryParam("amount", 50000)
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    private long createReservation(Scenario.BookableSlot slot) {
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(1).toString(),
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId());

        return RestAssured.given()
                .header(AUTHORIZATION, slot.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private String orderIdOf(String bearer, long reservationId) {
        return RestAssured.given()
                .header(AUTHORIZATION, bearer)
                .queryParam("reservationId", reservationId)
                .when().get("/order")
                .then().statusCode(200)
                .extract().jsonPath().getString("orderId");
    }
}