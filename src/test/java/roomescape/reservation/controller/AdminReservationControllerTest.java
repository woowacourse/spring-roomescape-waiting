package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import java.time.LocalDate;
import java.util.Map;
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
class AdminReservationControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findReservationTest() {
        Cookies cookies = makeAdminCookie();
        Map<String, String> parameters = Map.of("themeId", "1");
        int expected = jdbcTemplate.queryForObject(
                "SELECT count(1) from reservation WHERE theme_id = 1", Integer.class);

        int size = RestAssured.given().log().all()
                .cookies(cookies)
                .params(parameters)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(expected);
    }

    @DisplayName("예약을 DB에 추가할 수 있다.")
    @Test
    void createReservationTest() {
        ReservationCreateRequest params = new ReservationCreateRequest(
                2L, LocalDate.now().plusDays(7), 1L, 1L);
        Cookies cookies = makeAdminCookie();

        RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    private Cookies makeAdminCookie() {
        LoginRequest request = new LoginRequest("admin@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }
}
