package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.TossPaymentException;

class PaymentControllerE2ETest extends BaseE2ETest {

    @MockBean
    private PaymentGateway paymentGateway;

    private String userSession;
    private Long timeId;
    private Long themeId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        seedMember("유저", "user@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A", 30000L);
        userSession = loginAs("user@test.com");
        given(paymentGateway.confirm(any())).willAnswer(invocation -> {
            PaymentConfirmation c = invocation.getArgument(0);
            return new PaymentResult(c.paymentKey(), c.orderId(), "DONE", c.amount());
        });
    }

    private Response createReservation() {
        Map<String, Object> body = new HashMap<>();
        body.put("date", LocalDate.now().plusDays(1).toString());
        body.put("timeId", timeId);
        body.put("themeId", themeId);
        body.put("storeId", storeId);
        return RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON).body(body)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().response();
    }

    @Test
    @DisplayName("금액이 일치하면 승인되어 예약이 BOOKED로 확정된다")
    void confirmSuccess() {
        Response created = createReservation();
        String orderId = created.path("orderId");
        int amount = created.path("amount");
        Long reservationId = ((Number) created.path("reservationId")).longValue();

        RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "pk-1", "orderId", orderId, "amount", amount))
                .when().post("/payments/confirm")
                .then().statusCode(HttpStatus.OK.value());

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM reservations WHERE id = ?", String.class, reservationId);
        assertThat(status).isEqualTo("BOOKED");
        verify(paymentGateway).confirm(any());
    }

    @Test
    @DisplayName("게이트웨이가 카드 거절을 던지면 그 status(403)로 응답한다")
    void confirmCardRejected() {
        Response created = createReservation();
        String orderId = created.path("orderId");
        int amount = created.path("amount");
        willThrow(new TossPaymentException.CardRejected("한도초과 또는 잔액부족"))
                .given(paymentGateway).confirm(any());

        RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "pk-1", "orderId", orderId, "amount", amount))
                .when().post("/payments/confirm")
                .then().statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("조작된 금액은 400으로 차단되고 게이트웨이가 호출되지 않는다")
    void confirmAmountMismatch() {
        Response created = createReservation();
        String orderId = created.path("orderId");

        RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON)
                .body(Map.of("paymentKey", "pk-1", "orderId", orderId, "amount", 9999))
                .when().post("/payments/confirm")
                .then().statusCode(HttpStatus.BAD_REQUEST.value());

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    @DisplayName("실패 콜백은 결제 대기 주문/예약을 정리한다(CANCELED)")
    void failCleansUp() {
        Response created = createReservation();
        String orderId = created.path("orderId");
        Long reservationId = ((Number) created.path("reservationId")).longValue();

        RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON)
                .body(Map.of("code", "PAY_PROCESS_CANCELED", "message", "사용자 취소", "orderId", orderId))
                .when().post("/payments/fail")
                .then().statusCode(HttpStatus.OK.value());

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM reservations WHERE id = ?", String.class, reservationId);
        assertThat(status).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("orderId 없는 취소도 NPE 없이 200으로 처리된다")
    void failWithoutOrderId() {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "PAY_PROCESS_CANCELED");
        body.put("message", "사용자 취소");
        body.put("orderId", null);

        RestAssured.given()
                .sessionId(userSession).contentType(ContentType.JSON)
                .body(body)
                .when().post("/payments/fail")
                .then().statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("타인의 주문을 실패 처리하려 하면 404로 차단되고 피해자 예약은 보존된다")
    void failByOtherMemberIsBlocked() {
        Response created = createReservation();
        String orderId = created.path("orderId");
        Long reservationId = ((Number) created.path("reservationId")).longValue();
        seedMember("타인", "other@test.com", "USER");
        String otherSession = loginAs("other@test.com");

        RestAssured.given()
                .sessionId(otherSession).contentType(ContentType.JSON)
                .body(Map.of("code", "PAY_PROCESS_CANCELED", "message", "x", "orderId", orderId))
                .when().post("/payments/fail")
                .then().statusCode(HttpStatus.NOT_FOUND.value());

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM reservations WHERE id = ?", String.class, reservationId);
        assertThat(status).isEqualTo("PENDING");
    }
}
