package roomescape.time.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("시간 컨트롤러")
class ReservationTimeControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("시간 컨트롤러는 시간 추가 요청이 들어오면 저장 후 200을 반환한다.")
    @Test
    void createTime() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:30");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(200)
                .body("size()", is(3));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(200);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(200)
                .body("size()", is(4));
    }

    @DisplayName("시간 컨트롤러는 중복된 시간 추가 요청이 들어오면 400을 반환한다.")
    @Test
    void createTimeWithDuplicatedStartAt() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:30");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then()
                .statusCode(200);

        // when
        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("중복된 예약 시간입니다.");
    }

    @DisplayName("시간 컨트롤러는 시간 생성 시 잘못된 형식의 본문이 들어오면 400을 응답한다.")
    @Test
    void createInvalidRequestBody() {
        String invalidBody = "invalidBody";

        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(invalidBody)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        assertThat(detailMessage).isEqualTo("요청에 잘못된 형식의 값이 있습니다.");
    }

    @DisplayName("시간 컨트롤러는 잘못된 형식의 시간으로 시간 생성 요청 시 400을 응답한다.")
    @ValueSource(strings = {"aaa", "10:000", "25:30"})
    @ParameterizedTest
    void createInvalidTimeReservationTime(String invalidTime) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", invalidTime);

        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        assertThat(detailMessage).isEqualTo("잘못된 형식의 날짜 혹은 시간입니다.");
    }

    @DisplayName("시간 컨트롤러는 시간 조회 요청이 들어오면 저장된 시간을 반환한다.")
    @Test
    void readTimes() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @DisplayName("시간 컨트롤러는 특정 날짜와 테마에 대한 시간 조회 요청이 들어오면 저장된 시간을 반환한다.")
    @Test
    void readTimesWithDateAndThemeId() {
        RestAssured.given().log().all()
                .queryParam("date", "2024-12-02")
                .queryParam("themeId", 2)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @DisplayName("시간 컨트롤러는 시간 삭제 요청이 들어오면 삭제 후 200을 반환한다.")
    @Test
    void deleteTime() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(200)
                .body("size()", is(3));

        RestAssured.given().log().all()
                .when().delete("/times/3")
                .then().log().all()
                .statusCode(200);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("시간 컨트롤러는 존재하지 않는 시간 삭제 요청이 들어오면 400을 반환한다.")
    @Test
    void deleteTimeWithNonExists() {
        // given
        RestAssured.given()
                .when().delete("/times/3")
                .then()
                .statusCode(200);

        // when
        String detailMessage = RestAssured.given().log().all()
                .when().delete("/times/3")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("존재하지 않는 예약 시간입니다.");
    }
}
