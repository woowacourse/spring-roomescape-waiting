package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminReservationControllerTest extends ControllerTest {

    @DisplayName("모든 사용자의 예약 내역이 모두 조회되어야한다.")
    @Test
    void 관리자_예약_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자는 예약을 id만으로 삭제할 수 있다")
    @Test
    void 관리자_예약_삭제_API() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/{id}", id)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 예약을 관리자 삭제하면 404")
    @Test
    void 존재하지_않는_예약_관리자_삭제하면_404() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/{id}", 999)
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약입니다."));
    }

    private long createReservation(String name, String date, long timeId, long themeId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationParams(name, date, timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private Map<String, Object> reservationParams(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        return params;
    }
}
