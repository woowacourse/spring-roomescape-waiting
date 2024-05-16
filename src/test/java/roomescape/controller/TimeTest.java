package roomescape.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-db-clean.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class TimeTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("time 페이지에 새로운 예약 정보를 추가, 조회, 삭제할 수 있다.")
    @Test
    void given_when_saveAndDeleteTimes_then_statusCodeIsOkay() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:10");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));

        RestAssured.given().log().all()
                .when().delete("/times/4")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("이미 등록된 시간을 등록하면 400 오류를 반환한다.")
    @Test
    void given_when_saveDuplicatedTime_then_statusCodeIsBadRequest() {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 이미 등록된 시간입니다"));
    }

    @DisplayName("비어있는 시간으로 등록하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void given_when_saveInvalidTime_then_statusCodeIsBadRequest(String invalidTime) {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", invalidTime);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 잘못된 시간입니다"));
    }

    @DisplayName("부적절한 양식으로 시간을 등록하는 경우 개발자가 정의한 문구로 400 오류를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"99:99", "10:00:01"})
    void given_when_saveInvalidTime_then_statusCodeIsBadRequestWithCustomMessage(String invalidTime) {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", invalidTime);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 적절하지 않은 입력값 입니다"));
    }

    @DisplayName("삭제하고자 하는 시간에 예약이 등록되어 있으면 400 오류를 반환한다.")
    @Test
    void given_when_deleteTimeIdRegisteredReservation_then_statusCodeIsBadRequest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 예약이 등록된 시간은 제거할 수 없습니다"));
    }
}
