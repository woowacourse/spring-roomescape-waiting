package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/popularity-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeTest {

    @Test
    @DisplayName("예약이 없는 경우 테스트")
    void readAvailableTime() {
        RestAssured.given().log().all()
                .queryParam("date", "2027-05-03")
                .queryParam("themeId", 7L)
                .when().get("/times")
                .then()
                .statusCode(200).log().all()
                .body("size()", is(2));;
    }

    @Test
    @DisplayName("예약이 존재하는 경우 테스트")
    void readAvailableTimeWithExistReservation() {

        RestAssured.given().log().all()
                .queryParam("date", "2026-06-05")
                .queryParam("themeId", 7L)
                .when().get("/times")
                .then()
                .statusCode(200).log().all()
                .body("size()", is(1));;
    }

    @Test
    @DisplayName("날짜 형식이 잘못되면 400을 반환한다.")
    void readAvailableTimeWithInvalidDate() {
        RestAssured.given().log().all()
                .queryParam("date", "bad-date")
                .queryParam("themeId", 7L)
                .when().get("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("themeId가 숫자가 아니면 400을 반환한다.")
    void readAvailableTimeWithInvalidThemeId() {
        RestAssured.given().log().all()
                .queryParam("date", "2026-05-03")
                .queryParam("themeId", "abc")
                .when().get("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("과거 날짜는 예약 가능한 시간을 반환하지 않는다.")
    void readAvailableTimeWithPastDate() {
        RestAssured.given().log().all()
                .queryParam("date", "2026-05-04")
                .queryParam("themeId", 7L)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("존재하지 않는 themeId면 400을 반환한다.")
    void readAvailableTimeWithNonExistentThemeId() {
        RestAssured.given().log().all()
                .queryParam("date", "2026-05-03")
                .queryParam("themeId", 999L)
                .when().get("/times")
                .then().log().all()
                .statusCode(400);
    }
}
