package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import roomescape.exception.ProblemType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

class PaymentApiTest extends AbstractE2eTest {

    private PaymentOrder createOrder() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        JsonPath response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath();

        return new PaymentOrder(response.getString("orderId"), response.getInt("id"));
    }

    @Test
    void 조작된_금액으로_승인하면_422를_반환한다() {
        String orderId = createOrder().orderId();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", "pk_1");
        body.put("amount", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/payments/" + orderId + "/confirmation")
                .then().log().all()
                .statusCode(422)
                .body("type", is(ProblemType.PAYMENT_AMOUNT_MISMATCH.uri().toString()));
    }

    @Test
    void 결제_실패_주문을_삭제하면_PENDING_예약과_주문을_정리한다() {
        PaymentOrder order = createOrder();

        RestAssured.given().log().all()
                .when().delete("/payments/" + order.orderId())
                .then().log().all()
                .statusCode(204);

        Integer reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                order.reservationId()
        );
        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE order_id = ?",
                Integer.class,
                order.orderId()
        );

        assertThat(reservationCount).isZero();
        assertThat(paymentCount).isZero();
    }

    private record PaymentOrder(String orderId, Integer reservationId) {
    }
}
