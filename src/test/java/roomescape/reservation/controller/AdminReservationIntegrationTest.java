package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("전체 예약 목록을 조회한다.")
    @Test
    void getAll() {
        createThemeAndTime();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationBody("라이"))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", notNullValue())
                .body("[0].name", is("라이"));
    }

    @DisplayName("예약을 생성한다.")
    @Test
    void create() {
        createThemeAndTime();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationBody("라이"))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("라이"));
    }

    @DisplayName("예약을 취소한다.")
    @Test
    void delete() {
        createThemeAndTime();

        long createdReservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationBody("라이"))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/" + createdReservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("없는 예약 취소 시 404 에러를 응답한다.")
    @Test
    void 없는_예약_삭제시_404_에러_응답() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("code", is("RESERVATION_NOT_FOUND"))
                .body("message", is("예약을 찾을 수 없습니다."));
    }

    private void createThemeAndTime() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeBody())
                .when().post("/themes")
                .then().statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeBody())
                .when().post("/times")
                .then().statusCode(201);
    }

    private static Map<String, Object> reservationBody(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("themeId", 1);
        body.put("timeId", 1);
        return body;
    }

    private static Map<String, String> themeBody() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "오리엔탈");
        body.put("description", "오리엔탈 설명");
        body.put("imageUrl", "https://example.com/oriental.png");
        return body;
    }

    private static Map<String, String> timeBody() {
        Map<String, String> body = new HashMap<>();
        body.put("startAt", "2030-06-01T10:00");
        body.put("endAt", "2030-06-01T12:00");
        return body;
    }
}
