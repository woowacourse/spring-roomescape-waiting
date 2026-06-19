package roomescape.controller;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class AdminReservationTimeControllerTest extends ControllerTestSupport {

    @Test
    void 예약_시간을_추가한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "10:00"))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/admin/times/1");
    }

    @Test
    void startAt이_null이면_400을_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of())
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_시간을_삭제한다() {
        int timeId = createTime("10:00");

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 존재하지_않는_시간을_삭제하면_404를_반환한다() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약이_존재하는_시간을_삭제하면_409를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        createReservation(createMember("브라운"), LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(409);
    }
}
