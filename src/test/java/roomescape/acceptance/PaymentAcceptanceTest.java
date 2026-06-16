package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;
import roomescape.domain.ReservationStatus;
import roomescape.domain.exception.DomainErrorCode;

class PaymentAcceptanceTest extends AcceptanceTest {

    @Test
    void 예약_생성시_결제대기_주문정보를_반환한다() {
        예약_시간과_테마를_생성한다();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams())
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo(ReservationStatus.PENDING.name()))
                .body("orderId", notNullValue())
                .body("amount", equalTo(50000));
    }

    @Test
    void 결제_승인시_예약이_확정된다() {
        예약_시간과_테마를_생성한다();
        Map<String, Object> pending = ReservationFixture.createPendingReservation("예약자", NOW_DATE, 1L, 1L);

        Map<String, Object> confirmParams = new HashMap<>();
        confirmParams.put("paymentKey", "payment_key");
        confirmParams.put("orderId", pending.get("orderId"));
        confirmParams.put("amount", ((Number) pending.get("amount")).longValue());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(confirmParams)
                .when().post("/payments/confirm")
                .then().log().all()
                .statusCode(200)
                .body("status", equalTo(ReservationStatus.CONFIRMED.name()));
    }

    @Test
    void 결제_금액이_다르면_승인_전에_차단된다() {
        예약_시간과_테마를_생성한다();
        Map<String, Object> pending = ReservationFixture.createPendingReservation("예약자", NOW_DATE, 1L, 1L);

        Map<String, Object> confirmParams = new HashMap<>();
        confirmParams.put("paymentKey", "payment_key");
        confirmParams.put("orderId", pending.get("orderId"));
        confirmParams.put("amount", 1000L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(confirmParams)
                .when().post("/payments/confirm")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH.name()));
    }

    @Test
    void 결제_실패_정리시_orderId가_없어도_NPE가_발생하지_않는다() {
        Map<String, Object> failParams = new HashMap<>();
        failParams.put("code", "PAY_PROCESS_CANCELED");
        failParams.put("message", "사용자가 결제를 취소했습니다.");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(failParams)
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 결제설정은_클라이언트키만_반환한다() {
        RestAssured.given().log().all()
                .when().get("/payments/config")
                .then().log().all()
                .statusCode(200)
                .body("clientKey", equalTo("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm"))
                .body("secretKey", nullValue());
    }

    private void 예약_시간과_테마를_생성한다() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
    }

    private Map<String, Object> reservationParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "예약자");
        params.put("date", NOW_DATE);
        params.put("timeId", 1L);
        params.put("themeId", 1L);
        return params;
    }
}
