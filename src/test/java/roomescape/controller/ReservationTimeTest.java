package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/testData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
                .queryParam("date", "2026-05-03")
                .queryParam("themeId", 7L)
                .when().get("/times")
                .then()
                .statusCode(200).log().all()
                .body("size()", is(1));;
    }
}
