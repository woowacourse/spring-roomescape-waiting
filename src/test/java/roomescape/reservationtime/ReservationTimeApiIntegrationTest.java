package roomescape.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

public class ReservationTimeApiIntegrationTest extends ControllerTestSupport {

    @Test
    @DisplayName("특정 날짜와 테마의 예약 가능 시간들을 조회할 수 있다.")
    void finds_available_reservation_times_by_date_and_theme() {
        String accessToken = loginUserToken();

        Map<String, Object> options = new HashMap<>();
        options.put("date", "2026-05-05");
        options.put("themeId", 1);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .params(options)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("WAITABLE"))
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 및 예약 생성 이후 예약 가능 시간을 재조회를 할 수 있다.")
    void updates_available_times_after_creating_reservation() {
        String userToken = loginUserToken();

        Map<String, Object> options = new HashMap<>();
        options.put("date", "2026-05-05");
        options.put("themeId", 4);

        // 2026-05-05 4번 테마 조회
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .params(options)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("RESERVABLE"))
                .statusCode(200);

        // 예약 생성
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201);

        // 특정날짜와_테마에_예약_가능_시간을_조회
        Map<String, Object> options1 = new HashMap<>();
        options1.put("date", "2026-05-05");
        options1.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .params(options1)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("WAITABLE"))
                .statusCode(200);
    }

    @Test
    @DisplayName("매니저는 시간 관리 API를 사용할 수 있다.")
    void manager_uses_reservation_time_management_api_successfully() {
        String accessToken = loginManagerToken();

        Map<String, String> params = new HashMap<>();
        params.put("startAt", "14:00");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/times")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(5));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/manager/times/5")
                .then().log().all()
                .statusCode(204);
    }
}
