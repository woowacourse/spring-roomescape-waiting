package roomescape.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationControllerTest {

    @Test
    void 전체예약_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(15));
    }

    @Test
    void 이름으로_전체예약_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/reservations/list?name=로치")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(13));
    }

    @Test
    void 시간존재예약_추가_성공() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "초록");
        params.put("themeId", 2L);
        params.put("date", "2027-05-12");
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 시간없음예약_추가_실패() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "초록");
        params.put("date", "2026-05-05");
        params.put("timeId", 15L);
        params.put("themeId", 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESERVATION_TIME_NOT_FOUND"))
                .body("message", equalTo("예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 예약삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 이름_빈칸으로_예약추가_예외발생() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", " ");
        params.put("themeId", 2L);
        params.put("date", "2027-05-12");
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"))
                .body("message", equalTo("요청 값이 올바르지 않습니다."));
    }

    @Test
    void 날짜_형식_오류로_예약추가_예외발생() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "워넬");
        params.put("themeId", 2L);
        params.put("date", "2026/05/12");
        params.put("timeId", 7L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_DATE_FORMAT"))
                .body("message", equalTo("날짜 형식이 잘못되었습니다. (yyyy-MM-dd)"));
    }

    @Test
    void 본인_예약_변경_성공() {
        Map<String, Object> params = Map.of(
                "name", "로치",
                "themeId", 1L,
                "date", "2026-05-20",
                "timeId", 2L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("로치"))
                .body("date", is("2026-05-20"))
                .body("time.id", is(2));
    }

    @Test
    void 다른_사람의_예약은_변경할_수_없다() {
        Map<String, Object> params = Map.of(
                "name", "브라운",
                "themeId", 1L,
                "date", "2026-05-20",
                "timeId", 2L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(403)
                .body("code", is("CANNOT_MODIFY_OTHER_RESERVATION"));
    }

    @Test
    void 이미_예약된_시간으로는_변경할_수_없다() {
        Map<String, Object> params = Map.of(
                "name", "로치",
                "themeId", 1L,
                "date", "2026-05-20",
                "timeId", 3L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESERVATION_ALREADY_EXISTS"));
    }

    @Test
    void 본인_예약_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/reservations/my/1?name=로치")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 본인이_아닌_예약_삭제_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/my/1?name=브라운")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 없는_예약_삭제_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/my/17?name=로치")
                .then().log().all()
                .statusCode(404);
    }
}
