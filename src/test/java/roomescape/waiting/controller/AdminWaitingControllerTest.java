package roomescape.waiting.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminWaitingControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("모든 예약 대기를 조회할 수 있다.")
    @Test
    void findWaitingsTest() {
        Cookies cookies = makeAdminCookie();
        int expected = jdbcTemplate.queryForObject(
                "SELECT count(1) from waiting", Integer.class);

        int size = RestAssured.given().log().all()
                .cookies(cookies)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(expected);
    }

    @DisplayName("특정 예약 대기를 삭제할 수 있다.")
    @Test
    void deleteWaitingTest() {
        Cookies cookies = makeAdminCookie();

        RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .when().delete("/admin/waitings/1")
                .then().log().all()
                .statusCode(204);
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
