package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.payment.FakePaymentGatewayConfig;
import roomescape.payment.exception.PaymentErrorCode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(FakePaymentGatewayConfig.class)
@Sql(value = "/empty.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class PaymentAcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        createTime();
        createTheme();
    }

    private void createTime() {
        Map<String, String> timeParams = new HashMap<>();
        timeParams.put("startAt", "10:00");
        RestAssured.given().contentType(ContentType.JSON).body(timeParams)
                .when().post("/api/admin/times")
                .then().statusCode(201);
    }

    private void createTheme() {
        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "귀신찾기");
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");
        RestAssured.given().contentType(ContentType.JSON).body(themeParams)
                .when().post("/api/admin/themes")
                .then().statusCode(201);
    }

    private Response reserve(String name) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        return RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/api/reservations");
    }

    @Test
    void 결제_승인까지_성공하면_예약이_확정된다() {
        // given : 결제 대기 예약을 만들고 주문 정보를 받는다
        Response reserved = reserve("브라운");
        reserved.then().statusCode(201).body("status", is("PENDING"));
        String orderId = reserved.jsonPath().getString("payment.orderId");
        int amount = reserved.jsonPath().getInt("payment.amount");

        // when : 콜백 정보로 결제를 승인한다
        Map<String, Object> confirm = new HashMap<>();
        confirm.put("paymentKey", "test_payment_key");
        confirm.put("orderId", orderId);
        confirm.put("amount", amount);

        RestAssured.given().contentType(ContentType.JSON).body(confirm)
                .when().post("/api/payments/confirm")
                .then().statusCode(200);

        // then : 예약이 CONFIRMED로 확정된다
        RestAssured.given()
                .queryParam("name", "브라운")
                .when().get("/api/reservations")
                .then().statusCode(200)
                .body("reservations[0].status", is("CONFIRMED"));
    }

    @Test
    void 조작된_금액은_승인_전에_차단된다() {
        // given
        Response reserved = reserve("브라운");
        String orderId = reserved.jsonPath().getString("payment.orderId");
        int amount = reserved.jsonPath().getInt("payment.amount");

        // when : 콜백 금액을 조작한다
        Map<String, Object> tampered = new HashMap<>();
        tampered.put("paymentKey", "test_payment_key");
        tampered.put("orderId", orderId);
        tampered.put("amount", amount + 10000);

        // then : 금액 불일치로 차단된다
        RestAssured.given().contentType(ContentType.JSON).body(tampered)
                .when().post("/api/payments/confirm")
                .then()
                .statusCode(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.getHttpStatus().value())
                .body("code", is(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.getErrorName()));
    }
}
