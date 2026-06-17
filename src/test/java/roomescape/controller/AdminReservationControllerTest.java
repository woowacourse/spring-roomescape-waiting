package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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

    @DisplayName("대기가 없으면 빈 목록을 반환한다.")
    @Test
    void 관리자_대기_목록_없을때_빈_목록() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("waitings", hasSize(0));
    }

    @DisplayName("대기가 있으면 waitingNumber와 함께 반환한다.")
    @Test
    void 관리자_대기_목록_조회() {
        String futureDate = LocalDate.now().plusDays(5).toString();

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "김철수", "date", futureDate, "timeId", 1, "themeId", 1))
                .when().post("/reservations").then().statusCode(201);

        int waitingId = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "이영희", "date", futureDate, "timeId", 1, "themeId", 1))
                .when().post("/reservations/waiting")
                .then().statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("waitings", hasSize(1));
    }

    @DisplayName("어드민이 대기를 취소하면 204를 반환한다.")
    @Test
    void 관리자_대기_취소() {
        String futureDate = LocalDate.now().plusDays(5).toString();

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "김철수", "date", futureDate, "timeId", 1, "themeId", 1))
                .when().post("/reservations").then().statusCode(201);

        int waitingId = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "이영희", "date", futureDate, "timeId", 1, "themeId", 1))
                .when().post("/reservations/waiting")
                .then().statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/waiting/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }
}
