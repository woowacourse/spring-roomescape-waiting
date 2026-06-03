package roomescape.time.controller;

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
class TimeIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("시간 슬롯 전체 목록을 조회한다.")
    @Test
    void getAll() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeBody())
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", notNullValue())
                .body("[0].startAt", is("2030-06-01T10:00"))
                .body("[0].endAt", is("2030-06-01T12:00"));
    }

    @DisplayName("시간 슬롯을 생성한다.")
    @Test
    void create() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeBody())
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("startAt", is("2030-06-01T10:00"))
                .body("endAt", is("2030-06-01T12:00"));
    }

    @DisplayName("시간 슬롯을 삭제한다.")
    @Test
    void delete() {
        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeBody())
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().delete("/times/" + timeId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("없는 시간 슬롯 삭제 시 404 에러를 응답한다.")
    @Test
    void 없는_시간_삭제시_404_에러_응답() {
        RestAssured.given().log().all()
                .when().delete("/times/999")
                .then().log().all()
                .statusCode(404)
                .body("code", is("TIME_NOT_FOUND"))
                .body("message", is("예약 시간을 찾을 수 없습니다."));
    }

    private static Map<String, String> timeBody() {
        Map<String, String> body = new HashMap<>();
        body.put("startAt", "2030-06-01T10:00");
        body.put("endAt", "2030-06-01T12:00");
        return body;
    }
}
