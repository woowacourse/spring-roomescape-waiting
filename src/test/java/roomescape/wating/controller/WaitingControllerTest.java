package roomescape.wating.controller;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import roomescape.reservation.service.ReservationService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WaitingControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                    LocalDate.of(2026, 5, 8)
                            .atStartOfDay(ZoneId.of("Asia/Seoul"))
                            .toInstant(),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Test
    void 테마_날짜_시간_예약자명으로_대기를_등록할_수_있다() {
        //given
        Map<String, String> body = Map.of(
                "name", "재키",
                "date", "2026-05-26",
                "timeId", "1",
                "themeId", "2"
        );

        //when
        final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings");

        //then
        response.then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue());
    }
}
