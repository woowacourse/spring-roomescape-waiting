package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.exception.ThemeErrorCode;

@SpringWebTest
class ReservationTimeIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("모든 예약 시간을 성공적으로 조회한다.")
    void readAll_Success() {
        createReservationTime("10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].startAt", is("10:00:00"));
    }

    @Test
    @DisplayName("예약 가능한 시간을 성공적으로 조회한다.")
    void readAvailable_Success() {
        createReservationTime("10:00");
        createTheme("테마", "설명", "url");

        RestAssured.given().log().all()
                .queryParam("themeId", "1")
                .queryParam("date", "2026-05-05")
                .contentType(ContentType.JSON)
                .when().get("/times/available-times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].startAt", is("10:00:00"))
                .body("[0].alreadyBooked", is(false));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 파라미터가 누락되면 400 에러를 반환한다.")
    void readAvailable_MissingParams_BadRequest() {
        RestAssured.given().log().all()
                .queryParam("date", "2026-05-05")
                .contentType(ContentType.JSON)
                .when().get("/times/available-times")
                .then().log().all()
                .statusCode(400)
                .body("message", is("필수 요청 파라미터가 누락되었습니다. 입력 값을 다시 확인해 주세요."));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 테마가 존재하지 않으면 404 에러를 반환한다.")
    void readAvailable_ThemeNotFound_NotFound() {
        RestAssured.given().log().all()
                .queryParam("themeId", "9999")
                .queryParam("date", "2026-05-05")
                .contentType(ContentType.JSON)
                .when().get("/times/available-times")
                .then().log().all()
                .statusCode(404)
                .body("message", is(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));
    }
}
