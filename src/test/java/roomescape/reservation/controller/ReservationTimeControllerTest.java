package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerTest {
    @DisplayName("예약 시간을 정상적으로 추가한다.")
    @Test
    void save() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 가능 시간들을 조회한다.")
    @Test
    void getAll() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times");

        // when & then
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @DisplayName("예약 가능 시간을 삭제한다.")
    @Test
    void delete() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times");

        RestAssured.given().log().all()
                .when().delete("/times/4")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("존재하지 않은 예약 가능 시간을 삭제하려는 경우 실패한다.")
    @Test
    void doesNotExistsToDelete() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times");

        // when & then
        RestAssured.given().log().all()
                .when().delete("/times/2")
                .then().log().all()
                .statusCode(400);
    }
}
