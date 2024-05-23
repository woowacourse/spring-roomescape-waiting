package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.auth.dto.LoginRequest;
import roomescape.reservation.dto.ReservationCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationControllerTest {
    private static final int COUNT_OF_RESERVATION = 5;
    private static final int BRI_COUNT_OF_RESERVATION = 1;

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("예약 목록을 읽을 수 있다.")
    @Test
    void findReservations() {
        int size = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(COUNT_OF_RESERVATION);
    }

    @DisplayName("로그인한 사용자의 예약 목록을 읽을 수 있다.")
    @Test
    void findMyReservations() {
        Cookies userCookies = makeUserCookie();

        int size = RestAssured.given().log().all()
                .cookies(userCookies)
                .when().get("/reservations/accounts")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(BRI_COUNT_OF_RESERVATION);
    }

    @DisplayName("예약을 DB에 추가할 수 있다.")
    @Test
    void createReservation() {
        ReservationCreateRequest params = new ReservationCreateRequest(
                null, LocalDate.of(2040, 8, 5), 1L, 1L);
        long expectedId = COUNT_OF_RESERVATION + 1;
        Cookies userCookies = makeUserCookie();

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/" + expectedId);
    }

    private Cookies makeUserCookie() {
        LoginRequest request = new LoginRequest("bri@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }

    @DisplayName("삭제할 id를 받아서 DB에서 해당 예약을 삭제 할 수 있다.")
    @Test
    void deleteReservation() {
        RestAssured.given().log().all()
                .when().delete("/reservations/5")
                .then().log().all()
                .statusCode(204);
    }
}
