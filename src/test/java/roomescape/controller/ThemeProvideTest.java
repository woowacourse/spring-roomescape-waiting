package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/testData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeProvideTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        public Clock testClock() {
            return Clock.fixed(
                    LocalDate.of(2026, 5, 5).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
        }
    }

    @Test
    @DisplayName("개수 테스트")
    void readAvailableTime() {
        RestAssured.given().log().all()
                    .queryParam("limit", 10)
                    .when().get("/themes/popular")
                    .then().statusCode(200)
                    .log().all()
                    .body("size()", is(10));
    }

    @Test
    @DisplayName("오름차순 테스트")
    void sortThemes() {
        RestAssured.given().log().all()
                .queryParam("limit", 10)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10))
                .body("[0].name", is("공포의 저택"))
                .body("[1].name", is("우주 탐험대"))
                .body("[2].name", is("탐정 사무소"))
                .body("[3].name", is("마법사의 탑"))
                .body("[4].name", is("해적선"))
                .body("[5].name", is("고대 신전"))
                .body("[6].name", is("좀비 연구소"))
                .body("[7].name", is("타임머신"))
                .body("[8].name", is("사막의 오아시스"))
                .body("[9].name", is("폐광"));
    }
}
