package roomescape.waiting.controller;

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
import roomescape.waiting.dto.WaitingRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingControllerTest {
    private static final int COUNT_OF_WAITING = 2;

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("이미 존재하는 예약에 대해, 예약 대기를 할 수 있다.")
    @Test
    void createWaitingTest() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2050, 5, 5), 2L, 2L);
        long expectedId = COUNT_OF_WAITING + 1;
        Cookies userCookies = makeUserCookie("bri@abc.com", "1234");

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waitings/" + expectedId);
    }

    @DisplayName("예약 대기 주인에 한하여, 예약 대기를 취소할 수 있다.")
    @Test
    void deleteWaitingTest() {
        Cookies userCookies = makeUserCookie("duck@abc.com", "1234");

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from waiting", Integer.class);
        assertThat(countAfterDelete).isEqualTo(COUNT_OF_WAITING - 1);
    }

    private Cookies makeUserCookie(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }
}
