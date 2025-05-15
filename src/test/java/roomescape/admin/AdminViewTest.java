package roomescape.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdminViewTest {

    private static final Map<String, Object> AUTH_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public AdminViewTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeAll
    static void beforeAll() {
        AUTH_BODY.put("email", "admin@email.com");
        AUTH_BODY.put("password", "password");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "admin@email.com", "password", "name", "ADMIN");
    }

    @DisplayName("/admin으로 요청이 들어오면 어드민 페이지를 응답한다.")
    @Test
    void admin() {
        // given
        final Cookie cookie = givenAuthCookie();

        // when & then
        RestAssured.given().port(port).log().all()
                .cookie(cookie)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/reservation으로 요청이 들어오면 예약 페이지를 응답한다.")
    @Test
    void adminReservation() {
        // given
        final Cookie cookie = givenAuthCookie();

        // when & then
        RestAssured.given().port(port).log().all()
                .cookie(cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/time으로 요청이 들어오면 시간 설정 페이지를 응답한다.")
    @Test
    void adminTime() {
        // given
        final Cookie cookie = givenAuthCookie();

        // when & then
        RestAssured.given().port(port).log().all()
                .cookie(cookie)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/theme으로 요청이 들어오면 테마 설정 페이지를 응답한다.")
    @Test
    void adminTheme() {
        // given
        final Cookie cookie = givenAuthCookie();

        // when & then
        RestAssured.given().port(port).log().all()
                .cookie(cookie)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    private Cookie givenAuthCookie() {
        return RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(AUTH_BODY)
                .when().post("/login")
                .then()
                .extract().detailedCookie("token");
    }
}
