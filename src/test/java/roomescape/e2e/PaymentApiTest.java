package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.exception.ProblemType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

class PaymentApiTest extends AbstractE2eTest {

    private String createOrder() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getString("orderId");
    }

    @Test
    void 조작된_금액으로_승인하면_422를_반환한다() {
        String orderId = createOrder();

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
}
