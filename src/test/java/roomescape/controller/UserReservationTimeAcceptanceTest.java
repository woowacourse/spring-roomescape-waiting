package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/popular-theme-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserReservationTimeAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("시간이 2개일 때, 예약 가능한 모든 시간 조회 기능")
    class AvailableTimeCases {
        @Test
        @DisplayName("예약이 없는 날짜의 시간을 조회한다.")
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
        @DisplayName("예약이 하나 존재하는 날짜의 시간을 조회한다.")
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
}
