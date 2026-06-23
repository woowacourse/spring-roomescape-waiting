package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class TimeControllerTest {

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("예약 시간 등록 API")
    @Test
    void 예약_시간_등록_API() {
        final String createStartAt = "23:00";
        final Map<String, Object> params = new HashMap<>();
        params.put("startAt", createStartAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(createStartAt));
    }

    @DisplayName("예약 시간 등록 API - 이상값 예외 테스트")
    @Test
    void 예약_시간_등록_API_예외_테스트() {
        final String createStartAt = "230";
        final Map<String, Object> params = new HashMap<>();
        params.put("startAt", createStartAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @DisplayName("예약 시간 조회 API")
    @Test
    void 예약_시간_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(1))
                .body("[0].startAt", equalTo("10:00"))
                .body("[12].id", equalTo(13))
                .body("[12].startAt", equalTo("22:00"));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("API - 예약 시간 삭제")
    @Test
    void API_예약_시간_삭제() {
        final String createStartAt = "23:00";
        final Map<String, Object> params = new HashMap<>();
        params.put("startAt", createStartAt);

        final long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(createStartAt))
                .extract()
                .jsonPath()
                .getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/" + id)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", equalTo(13));
    }
}
