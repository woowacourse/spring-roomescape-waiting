package roomescape.waiting.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.CookieProvider;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.waiting.dto.WaitingCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("이미 예약되어 있는 방탈출에 대해 예약 대기를 할 수 있다.")
    @Test
    void createReservation() {
        ReservationCreateRequest reservationParams = new ReservationCreateRequest(
                null, LocalDate.of(2040, 8, 5), 1L, 1L);
        Cookies adminCookies = CookieProvider.makeAdminCookie();

        RestAssured.given().log().all()
                .cookies(adminCookies)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        WaitingCreateRequest waitingParams = new WaitingCreateRequest(
                LocalDate.of(2040, 8, 5), 1L, 1L);
        long expectedId = 1L;
        Cookies userCookies = CookieProvider.makeUserCookie();

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waitings/" + expectedId);
    }
}
