package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class ReservationTest {

    @Test
    @DisplayName("사용자 예약 요청은 결제 대기 예약을 생성한다.")
    void createPendingReservationTest() {

        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);

        int newId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("reservationId", notNullValue())
                .body("name", is("녀녕"))
                .body("date", is("2026-06-05"))
                .body("status", is("PENDING_PAYMENT"))
                .body("payment.orderId", notNullValue())
                .body("payment.amount", is(5_000))
                .extract().path("reservationId");

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4))
                .body("find { it.id == " + newId + " }.name", is("녀녕"))
                .body("find { it.id == " + newId + " }.date", is("2026-06-05"))
                .body("find { it.id == " + newId + " }.status", is("결제대기"));
    }

    @Test
    @DisplayName("이전 시간에 대해서는 예약을 생성할 수 없다.")
    void pastReservationTest() {

        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-04-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("관리자 예약 삭제 시 같은 슬롯의 대기가 결제대기 예약으로 전환된다.")
    void deleteReservationTest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/3")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("find { it.id == 3 }", nullValue())
                .body("find { it.name == 'user_e' && it.date == '2026-06-05' }.status", is("결제대기"));
    }
}
